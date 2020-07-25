package com.flyersoft.source.manager;

import android.text.TextUtils;

import com.flyersoft.source.bean.BookChapterBean;
import com.flyersoft.source.bean.BookContentBean;
import com.flyersoft.source.bean.BookShelfBean;
import com.flyersoft.source.bean.BookSource;
import com.flyersoft.source.bean.SearchBookBean;
import com.flyersoft.source.dao.SourceController;
import com.flyersoft.source.manager.analyzeRule.AnalyzeHeaders;
import com.flyersoft.source.manager.analyzeRule.AnalyzeUrl;
import com.flyersoft.source.manager.content.BookChapterList;
import com.flyersoft.source.manager.content.BookContent;
import com.flyersoft.source.manager.content.BookInfo;
import com.flyersoft.source.manager.content.BookList;
import com.flyersoft.source.utils.NetworkUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;
import static com.flyersoft.source.conf.Consts.JS_PATTERN;

/**
 * Created By huzheng
 * Date 2020/5/7
 * Des 书籍爬虫
 */
public class BookModel extends BaseModel {

    private String tag;
    private String name;
    private BookSource bookSource;
    private Map<String, String> headerMap;
    public int retryModel = SEARCH_MODEL_DEFAULT;//网络错误进行重试

    public static BookModel getInstance(String tag) {
        return new BookModel(tag);
    }

    private BookModel(String tag) {
        this.tag = tag;
        try {
            URL url = new URL(tag);
            name = url.getHost();
        } catch (MalformedURLException e) {
            name = tag;
        }
        bookSource = SourceController.getInstance().getBookSourceByUrl(tag);
        if (bookSource != null) {
            name = bookSource.getBookSourceName();
            headerMap = AnalyzeHeaders.getMap(bookSource);
        }
    }

    /**
     * 发现书籍
     */
    public Observable<List<SearchBookBean>> findBook(String url, int page) {
        if (bookSource == null) {
            return Observable.error(new Throwable("无效源：" + tag));
        }
        final BookList bookList = new BookList(tag, name, bookSource, true);
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(url, null, page, headerMap, tag);
            return getResponseO(analyzeUrl)
                    .flatMap(new Function<Response<String>, ObservableSource<List<SearchBookBean>>>() {
                        @Override
                        public ObservableSource<List<SearchBookBean>> apply(Response<String> stringResponse) throws Exception {
                            return bookList.analyzeSearchBook(stringResponse);
                        }
                    })
                    .map(new Function<List<SearchBookBean>, List<SearchBookBean>>() {
                        @Override
                        public List<SearchBookBean> apply(List<SearchBookBean> searchBookBeans) throws Exception {
                            for (SearchBookBean searchBookBean : searchBookBeans) {
                                searchBookBean.setCoverUrl(NetworkUtils.getAbsoluteURL(searchBookBean.getTag(), searchBookBean.getCoverUrl()));
                            }
                            return searchBookBeans;
                        }
                    });
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("%s错误:%s", url, e.getLocalizedMessage())));
        }
    }

    /**
     * 搜索
     *
     * @return
     */
    public Observable<List<SearchBookBean>> searchBook(final String content, final int page) {
        if (bookSource == null || isEmpty(bookSource.getRuleSearchUrl())) {
            return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
                @Override
                public void subscribe(ObservableEmitter<List<SearchBookBean>> emitter) throws Exception {
                    emitter.onNext(new ArrayList<SearchBookBean>());
                    emitter.onComplete();
                }
            });
        }
        final BookList bookList = new BookList(tag, name, bookSource, false);
        try {
            return Observable.just(page)
                    .map(new Function<Integer, AnalyzeUrl>() {
                        @Override
                        public AnalyzeUrl apply(Integer integer) throws Exception {
                            int p = page;
                            if (retryModel==SEARCH_MODEL_DEFAULT) {
                                p = page;
                            } else if(retryModel==SEARCH_MODEL_LOADMORE){
                                p = page + 1;
                            } else if (retryModel == SEARCH_MODEL_RELOAD) {
                                p = page;
                            }
                            AnalyzeUrl analyzeUrl = new AnalyzeUrl(bookSource.getRuleSearchUrl(), content, p, headerMap, tag);
                            return analyzeUrl;
                        }
                    })
                    .flatMap(new Function<AnalyzeUrl, ObservableSource<Response<String>>>() {
                        @Override
                        public ObservableSource<Response<String>> apply(AnalyzeUrl analyzeUrl) throws Exception {
                            return getResponseO(analyzeUrl);
                        }
                    })
                    .flatMap(new Function<Response<String>, ObservableSource<List<SearchBookBean>>>() {
                        @Override
                        public ObservableSource<List<SearchBookBean>> apply(Response<String> stringResponse) throws Exception {
                            return bookList.analyzeSearchBook(stringResponse);
                        }
                    });
        } catch (Exception e) {
            return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
                @Override
                public void subscribe(ObservableEmitter<List<SearchBookBean>> emitter) throws Exception {
                    emitter.onNext(new ArrayList<SearchBookBean>());
                }
            });
        }
    }

    /**
     * 获取书籍详情
     *
     * @param bookShelfBean
     * @return
     */
    public synchronized Observable<BookShelfBean> getInfo(final BookShelfBean bookShelfBean) {

        if (bookSource == null) {
            return Observable.error(new Throwable("未找到对应源"));
        }
        final BookInfo bookInfo = new BookInfo(tag, name, bookSource);
        if (!TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getBookInfoHtml())) {
            return bookInfo.analyzeBookInfo(bookShelfBean.getBookInfoBean().getBookInfoHtml(), bookShelfBean);
        }
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(bookShelfBean.getNoteUrl(), headerMap, tag);
            return getResponseO(analyzeUrl)
                    .flatMap(new Function<Response<String>, ObservableSource<Response<String>>>() {
                        @Override
                        public ObservableSource<Response<String>> apply(Response<String> stringResponse) throws Exception {
                            return setCookie(stringResponse, tag);
                        }
                    })
                    .flatMap(new Function<Response<String>, ObservableSource<BookShelfBean>>() {
                        @Override
                        public ObservableSource<BookShelfBean> apply(Response<String> stringResponse) throws Exception {
                            return bookInfo.analyzeBookInfo(stringResponse.body(), bookShelfBean);
                        }
                    });
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("url错误:%s", bookShelfBean.getNoteUrl())));
        }
    }


    /**
     * 获取目录
     */
    public Observable<List<BookChapterBean>> getChapterList(final BookShelfBean bookShelfBean) {
        if (bookSource == null) {
            return Observable.error(new Throwable("书源错误"));
        }
        final BookChapterList bookChapterList = new BookChapterList(tag, bookSource, true);
        if (!TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getChapterListHtml())) {
            return bookChapterList.analyzeChapterList(bookShelfBean.getBookInfoBean().getChapterListHtml(), bookShelfBean, headerMap);
        }
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(bookShelfBean.getBookInfoBean().getChapterUrl(), headerMap, bookShelfBean.getNoteUrl());
            return getResponseO(analyzeUrl)
                    .flatMap(new Function<Response<String>, ObservableSource<Response<String>>>() {
                        @Override
                        public ObservableSource<Response<String>> apply(Response<String> stringResponse) throws Exception {
                            return setCookie(stringResponse, tag);
                        }
                    })
                    .flatMap(new Function<Response<String>, ObservableSource<List<BookChapterBean>>>() {
                        @Override
                        public ObservableSource<List<BookChapterBean>> apply(Response<String> stringResponse) throws Exception {
                            return bookChapterList.analyzeChapterList(stringResponse.body(), bookShelfBean, headerMap);
                        }
                    });
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("url错误:%s", bookShelfBean.getBookInfoBean().getChapterUrl())));
        }
    }


    /**
     * 获取内容
     */
    public Observable<BookContentBean> getContent(final BookShelfBean bookShelfBean, final BookChapterBean chapterBean, final BookChapterBean nextChapterBean, final boolean isTest) {
        if (bookSource == null) {
            return Observable.error(new Throwable("书源错误"));
        }
        if (isEmpty(bookSource.getRuleBookContent())) {
            return Observable.create(new ObservableOnSubscribe<BookContentBean>() {
                @Override
                public void subscribe(ObservableEmitter<BookContentBean> emitter) throws Exception {
                    BookContentBean bookContentBean = new BookContentBean();
                    bookContentBean.setDurChapterContent(chapterBean.getDurChapterUrl());
                    bookContentBean.setDurChapterIndex(chapterBean.getDurChapterIndex());
                    bookContentBean.setTag(bookShelfBean.getTag());
                    bookContentBean.setDurChapterUrl(chapterBean.getDurChapterUrl());
                    emitter.onNext(bookContentBean);
                    emitter.onComplete();
                }
            });
        }
        final BookContent bookContent = new BookContent(tag, bookSource);
        if (Objects.equals(chapterBean.getDurChapterUrl(), bookShelfBean.getBookInfoBean().getChapterUrl())
                && !TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getChapterListHtml())) {
            return bookContent.analyzeBookContent(bookShelfBean.getBookInfoBean().getChapterListHtml(), chapterBean, nextChapterBean, bookShelfBean, headerMap, isTest);
        }
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapterBean.getDurChapterUrl(), headerMap, bookShelfBean.getBookInfoBean().getChapterUrl());
            String contentRule = bookSource.getRuleBookContent();
            if (contentRule.startsWith("$") && !contentRule.startsWith("$.")) {
                //动态网页第一个js放到webView里执行
                contentRule = contentRule.substring(1);
                String js = null;
                Matcher jsMatcher = JS_PATTERN.matcher(contentRule);
                if (jsMatcher.find()) {
                    js = jsMatcher.group();
                    if (js.startsWith("<js>")) {
                        js = js.substring(4, js.lastIndexOf("<"));
                    } else {
                        js = js.substring(4);
                    }
                }
                return getAjaxString(analyzeUrl, tag, js)
                        .flatMap(new Function<String, ObservableSource<BookContentBean>>() {
                            @Override
                            public ObservableSource<BookContentBean> apply(String response) throws Exception {
                                return bookContent.analyzeBookContent(response, chapterBean, nextChapterBean, bookShelfBean, headerMap, isTest);
                            }
                        });
            } else {
                return getResponseO(analyzeUrl)
                        .flatMap(new Function<Response<String>, ObservableSource<Response<String>>>() {
                            @Override
                            public ObservableSource<Response<String>> apply(Response<String> response) throws Exception {
                                return setCookie(response, tag);
                            }
                        })
                        .flatMap(new Function<Response<String>, ObservableSource<BookContentBean>>() {
                            @Override
                            public ObservableSource<BookContentBean> apply(Response<String> response) throws Exception {
                                return bookContent.analyzeBookContent(response, chapterBean, nextChapterBean, bookShelfBean, headerMap, isTest);
                            }
                        });
            }
        } catch (Exception e) {
            return Observable.error(new Throwable(String.format("url错误:%s", chapterBean.getDurChapterUrl())));
        }
    }


    public BookShelfBean bookShelf;
    public long startTime;
}
