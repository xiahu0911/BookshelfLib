package com.flyersoft.source.manager.engine;

import android.util.Log;

import com.flyersoft.source.bean.BookChapterBean;
import com.flyersoft.source.bean.BookContentBean;
import com.flyersoft.source.bean.BookShelfBean;
import com.flyersoft.source.bean.BookSource;
import com.flyersoft.source.bean.SearchBookBean;
import com.flyersoft.source.conf.Consts;
import com.flyersoft.source.dao.SourceController;
import com.flyersoft.source.manager.BaseModel;
import com.flyersoft.source.manager.BookModel;
import com.flyersoft.source.manager.content.BookList;
import com.flyersoft.source.utils.BookUtils;
import com.flyersoft.source.utils.Loger;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created By huzheng
 * Date 2020/5/13
 * Des 搜索引擎
 */
public class TestEngine {

    private ExecutorService executorService;//线程池
    private Scheduler scheduler;
    private CompositeDisposable compositeDisposable;//网络请求管理
    private List<BookSource> bookSources = new ArrayList<>();
    private int index = 0;

    public TestEngine() {
        executorService = Executors.newFixedThreadPool(6);
        scheduler = Schedulers.from(executorService);
        compositeDisposable = new CompositeDisposable();
        bookSources.clear();
        List<BookSource> allSelect = SourceController.getInstance().getAllSelect();
        for (BookSource bookSource : allSelect) {
            if (bookSource.getEnable()) {
                bookSources.add(bookSource);
            }
        }
    }

    public void setBookSources(List<BookSource> bookSources) {
        this.bookSources.clear();
        this.bookSources.addAll(bookSources);
    }

    /**
     * 测试源
     *
     * @param keyStr
     */
    public synchronized void test(final String keyStr, final TestEngineListener testListener) {

        index = 0;
        if (bookSources.size() == 0) {
            testListener.onError("没有任何源");
        }

        testListener.onStart(this);
        for (final BookSource bookSource : bookSources) {
            if (StringUtils.isEmpty(bookSource.getRuleSearchUrl())) {
                bookSource.setTestMsg("没有搜索地址");
                bookSource.setTestState(Consts.TEST_STATE_FAILD);
                testListener.onItemFinish(index, bookSources.size());
                index++;
                if (index >= bookSources.size()) {
                    testListener.onFinish();
                    index = 0;
                }
                continue;
            }
            if (bookSource.getBookSourceType().equals("0")) {
                testBook(keyStr, testListener, bookSource);
            }else {
                testMusic(keyStr, testListener, bookSource);
            }
        }
    }

    private void testMusic(final String keyStr, final TestEngineListener testListener, final BookSource bookSource) {
        final BookModel bookModel = BookModel.getInstance(bookSource.getBookSourceUrl());
        Observable.just(keyStr)
                .flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
                    @Override
                    public ObservableSource<List<SearchBookBean>> apply(String s) throws Exception {
                        Loger.H("开始：" + bookSource.getBookSourceName());
                        bookSource.setTestState(Consts.TEST_STATE_START);
                        bookSource.setTestMsg("校验搜索...");
                        bookModel.startTime = System.currentTimeMillis();
                        testListener.onItemStart(bookSource);
                        testListener.onNext(bookSource);
                        return bookModel.searchBook(s, 0)
                                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                                    //记录重试次数
                                    int retryCount = 0;

                                    @Override
                                    public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
                                        return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                                            @Override
                                            public ObservableSource<?> apply(Throwable throwable) throws Exception {
                                                //出现自定义错误时加载下一页，其他错误则重试
                                                if (throwable.getMessage().equals(BookList.SEARCH_ERR_NET)) {
                                                    bookModel.retryModel = BaseModel.SEARCH_MODEL_RELOAD;
                                                } else if (throwable.getMessage().equals(BookList.SEARCH_ERR_DATA)) {
                                                    bookModel.retryModel = BaseModel.SEARCH_MODEL_LOADMORE;
                                                } else {
                                                    bookModel.retryModel = BaseModel.SEARCH_MODEL_RELOAD;
                                                }
                                                //延时200毫秒重试
                                                return retryCount++ == 0 ? Observable.timer(200, TimeUnit.MILLISECONDS) : Observable.error(throwable);
                                            }
                                        });
                                    }
                                });
                    }
                })
                .map(new Function<List<SearchBookBean>, BookShelfBean>() {
                    @Override
                    public BookShelfBean apply(List<SearchBookBean> searchBookBeans) throws Exception {
                        //取第一本小说
                        return BookUtils.getBookFromSearchBook(searchBookBeans.get(0));
                    }
                })
                .flatMap(new Function<BookShelfBean, ObservableSource<BookShelfBean>>() {
                    @Override
                    public ObservableSource<BookShelfBean> apply(BookShelfBean bookShelfBean) throws Exception {
                        bookSource.setTestMsg("校验详情...");
                        testListener.onNext(bookSource);
                        return bookModel.getInfo(bookShelfBean);
                    }
                })
                .timeout(30, TimeUnit.SECONDS)
                .subscribeOn(scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BookShelfBean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        compositeDisposable.add(d);
                        bookSource.d = d;
                        bookSource.setTestState(Consts.TEST_STATE_DEFAULT);
                        bookSource.setTestMsg("");
                    }

                    @Override
                    public void onNext(@NonNull BookShelfBean bookShelfBean) {
                        Loger.H("成功：" + bookSource.getBookSourceName());
                        bookSource.setTestTime(System.currentTimeMillis() - bookModel.startTime);
                        bookSource.setTestMsg("校验成功：" + bookSource.getTestTime() + "毫秒");
                        bookSource.setTestState(Consts.TEST_STATE_SUCCESS);
                        index++;
                        testListener.onItemFinish(index, bookSources.size());
                        if (index >= bookSources.size()) {
                            testListener.onFinish();
                            index = 0;
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.i("MR2", "出错：" + bookSource.getBookSourceName());
                        bookSource.d = null;
                        bookSource.setTestState(Consts.TEST_STATE_FAILD);
                        if (e.getMessage() != null) {
                            if (e.getMessage().contains("connect") || e.getMessage().contains("Connection") || e.getMessage().contains("reset")) {
                                bookSource.setTestMsg("校验失败：无法连接");
                            } else {
                                bookSource.setTestMsg(bookSource.getTestMsg() + "(失败)");
                            }
                        } else {
                            bookSource.setTestMsg("校验失败：未知原因");
                        }
                        index++;
                        testListener.onItemFinish(index, bookSources.size());
                        if (index >= bookSources.size()) {
                            testListener.onFinish();
                            index = 0;
                        }
                    }

                    @Override
                    public void onComplete() {
                        Loger.H("结束：" + bookSource.getBookSourceName());
                        bookSource.d = null;
                    }
                });
    }

    private void testBook(final String keyStr, final TestEngineListener testListener, final BookSource bookSource) {
        final BookModel bookModel = BookModel.getInstance(bookSource.getBookSourceUrl());
        Observable.just(keyStr)
                .flatMap(new Function<String, ObservableSource<List<SearchBookBean>>>() {
                    @Override
                    public ObservableSource<List<SearchBookBean>> apply(String s) throws Exception {
                        Loger.H("开始：" + bookSource.getBookSourceName());
                        bookSource.setTestState(Consts.TEST_STATE_START);
                        bookSource.setTestMsg("校验搜索...");
                        bookModel.startTime = System.currentTimeMillis();
                        testListener.onItemStart(bookSource);
                        testListener.onNext(bookSource);
                        return bookModel.searchBook(s, 0)
                                .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                                    //记录重试次数
                                    int retryCount = 0;

                                    @Override
                                    public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
                                        return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                                            @Override
                                            public ObservableSource<?> apply(Throwable throwable) throws Exception {
                                                //出现自定义错误时加载下一页，其他错误则重试
                                                if (throwable.getMessage().equals(BookList.SEARCH_ERR_NET)) {
                                                    bookModel.retryModel = BaseModel.SEARCH_MODEL_RELOAD;
                                                } else if (throwable.getMessage().equals(BookList.SEARCH_ERR_DATA)) {
                                                    bookModel.retryModel = BaseModel.SEARCH_MODEL_LOADMORE;
                                                } else {
                                                    bookModel.retryModel = BaseModel.SEARCH_MODEL_RELOAD;
                                                }
                                                //延时200毫秒重试
                                                return retryCount++ == 0 ? Observable.timer(200, TimeUnit.MILLISECONDS) : Observable.error(throwable);
                                            }
                                        });
                                    }
                                });
                    }
                })
                .map(new Function<List<SearchBookBean>, BookShelfBean>() {
                    @Override
                    public BookShelfBean apply(List<SearchBookBean> searchBookBeans) throws Exception {
                        //取第一本小说
                        return BookUtils.getBookFromSearchBook(searchBookBeans.get(0));
                    }
                })
                .flatMap(new Function<BookShelfBean, ObservableSource<BookShelfBean>>() {
                    @Override
                    public ObservableSource<BookShelfBean> apply(BookShelfBean bookShelfBean) throws Exception {
                        bookSource.setTestMsg("校验详情...");
                        testListener.onNext(bookSource);
                        return bookModel.getInfo(bookShelfBean);
                    }
                })
                .flatMap(new Function<BookShelfBean, ObservableSource<List<BookChapterBean>>>() {
                    @Override
                    public ObservableSource<List<BookChapterBean>> apply(BookShelfBean bookShelfBean) throws Exception {
                        bookModel.bookShelf = bookShelfBean;
                        //获取章节
                        bookSource.setTestMsg("校验章节...");
                        testListener.onNext(bookSource);
                        return bookModel.getChapterList(bookShelfBean);
                    }
                })
                .map(new Function<List<BookChapterBean>, BookChapterBean>() {
                    @Override
                    public BookChapterBean apply(List<BookChapterBean> bookChapterBeans) throws Exception {
                        //取第一章
                        return bookChapterBeans.get(0);
                    }
                })
                .flatMap(new Function<BookChapterBean, ObservableSource<BookContentBean>>() {
                    @Override
                    public ObservableSource<BookContentBean> apply(BookChapterBean bookChapterBean) throws Exception {
                        //获取章节内容
                        bookSource.setTestMsg("校验内容...");
                        testListener.onNext(bookSource);
                        return bookModel.getContent(bookModel.bookShelf, bookChapterBean, null, true);
                    }
                })
                .timeout(30, TimeUnit.SECONDS)
                .subscribeOn(scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BookContentBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                        bookSource.d = d;
                        bookSource.setTestState(Consts.TEST_STATE_DEFAULT);
                        bookSource.setTestMsg("");
                    }

                    @Override
                    public void onNext(BookContentBean bookContentBean) {
                        Loger.H("成功：" + bookSource.getBookSourceName());
                        bookSource.setTestTime(System.currentTimeMillis() - bookModel.startTime);
                        bookSource.setTestMsg("校验成功：" + bookSource.getTestTime() + "毫秒");
                        bookSource.setTestState(Consts.TEST_STATE_SUCCESS);
                        index++;
                        testListener.onItemFinish(index, bookSources.size());
                        if (index >= bookSources.size()) {
                            testListener.onFinish();
                            index = 0;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("MR2", "出错：" + bookSource.getBookSourceName());
                        bookSource.d = null;
                        bookSource.setTestState(Consts.TEST_STATE_FAILD);
                        if (e.getMessage() != null) {
                            if (e.getMessage().contains("connect") || e.getMessage().contains("Connection") || e.getMessage().contains("reset")) {
                                bookSource.setTestMsg("校验失败：无法连接");
                            } else {
                                bookSource.setTestMsg(bookSource.getTestMsg() + "(失败)");
                            }
                        } else {
                            bookSource.setTestMsg("校验失败：未知原因");
                        }
                        index++;
                        testListener.onItemFinish(index, bookSources.size());
                        if (index >= bookSources.size()) {
                            testListener.onFinish();
                            index = 0;
                        }
                    }

                    @Override
                    public void onComplete() {
                        Loger.H("结束：" + bookSource.getBookSourceName());
                        bookSource.d = null;
                    }
                });
    }


    public interface TestEngineListener {
        //开始测试
        void onStart(TestEngine testEngine);

        void onItemStart(BookSource bookSource);

        void onNext(BookSource bookSource);

        void onItemFinish(int count, int max);

        //测试完
        void onFinish();

        //出错
        void onError(String e);
    }

    public void onDestory() {
        executorService.shutdown();
        scheduler.shutdown();
        compositeDisposable.dispose();
        for (BookSource bookSource : bookSources) {
//            if (bookSource.d != null && !bookSource.d.isDisposed()) {
//                bookSource.d.dispose();
            if (bookSource.getTestState() == Consts.TEST_STATE_START) {
                bookSource.setTestMsg("已停止");
                bookSource.setTestState(Consts.TEST_STATE_DEFAULT);
                Loger.H("设置任务状态: " + bookSource.getBookSourceName());
            }
//            }
        }
    }

}
