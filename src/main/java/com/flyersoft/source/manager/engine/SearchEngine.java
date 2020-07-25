package com.flyersoft.source.manager.engine;

import com.flyersoft.source.bean.BookSource;
import com.flyersoft.source.bean.SearchBookBean;
import com.flyersoft.source.dao.SourceController;
import com.flyersoft.source.manager.BaseModel;
import com.flyersoft.source.manager.BookModel;
import com.flyersoft.source.manager.content.BookList;

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
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created By huzheng
 * Date 2020/5/13
 * Des 搜索引擎
 */
public class SearchEngine {

    private ExecutorService executorService;//线程池
    private Scheduler scheduler;
    private CompositeDisposable compositeDisposable;//网络请求管理
    private List<BookSource> bookSources = new ArrayList<>();
    private int currentIndex = 0;//统计完成进度

    public SearchEngine(int num) {
        executorService = Executors.newFixedThreadPool(num);
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

    public synchronized void search(final String keyStr, final int page, final SearchEngineListener searchEngineListener) {

        if (bookSources.size() == 0) {
            searchEngineListener.onError("没有选中任何源");
        }

        for (BookSource bookSource : bookSources) {
            final BookModel bookModel = BookModel.getInstance(bookSource.getBookSourceUrl());
            bookModel.searchBook(keyStr, page)
                    .timeout(15, TimeUnit.SECONDS)
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
                    })
                    .subscribeOn(scheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<SearchBookBean>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            compositeDisposable.add(d);
                        }

                        @Override
                        public void onNext(List<SearchBookBean> searchBookBeans) {
                            currentIndex++;
                            if (searchBookBeans != null && searchBookBeans.size() > 0) {
                                searchEngineListener.onSuccess(searchBookBeans);
                            }
                            if (currentIndex == bookSources.size()) {
                                searchEngineListener.onFinish();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            currentIndex++;
                            if (currentIndex == bookSources.size()) {
                                searchEngineListener.onFinish();
                            }
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
        }
    }

    public interface SearchEngineListener {
        void onSuccess(List<SearchBookBean> data);

        void onFinish();

        void onError(String e);
    }

    public void onDestory() {
        executorService.shutdown();
        compositeDisposable.dispose();
    }

}
