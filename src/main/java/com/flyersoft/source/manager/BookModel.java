package com.flyersoft.source.manager;

import android.text.TextUtils;

import com.flyersoft.source.bean.BookChapterBean;
import com.flyersoft.source.bean.BookContentBean;
import com.flyersoft.source.bean.BookInfoBean;
import com.flyersoft.source.bean.BookShelfBean;
import com.flyersoft.source.bean.BookSource;
import com.flyersoft.source.bean.SearchBookBean;
import com.flyersoft.source.conf.Consts;
import com.flyersoft.source.dao.SourceController;
import com.flyersoft.source.manager.analyzeRule.AnalyzeHeaders;
import com.flyersoft.source.manager.analyzeRule.AnalyzeUrl;
import com.flyersoft.source.manager.content.BookChapterList;
import com.flyersoft.source.manager.content.BookContent;
import com.flyersoft.source.manager.content.BookInfo;
import com.flyersoft.source.manager.content.BookList;
import com.flyersoft.source.utils.Loger;
import com.flyersoft.source.utils.NetworkUtils;
import com.flyersoft.source.yuedu3.CallBack;
import com.flyersoft.source.yuedu3.WebBook;

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
import static com.flyersoft.source.conf.Consts.YUEDU_2_0;
import static com.flyersoft.source.conf.Consts.YUEDU_3_0;

/**
 * Created By huzheng
 * Date 2020/5/7
 * Des 书籍爬虫
 */
public class BookModel extends BaseModel {

    private String tag;
    private String name;
    public BookSource bookSource;
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
    public Observable<List<SearchBookBean>> findBook(final String url, final int page) {
        if (bookSource == null) {
            return Observable.error(new Throwable("无效源：" + tag));
        }
        //区分3.0
        if (bookSource.getFrom() == YUEDU_3_0) {
            return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
                @Override
                public void subscribe(final ObservableEmitter<List<SearchBookBean>> emitter) throws Exception {
                    try {
                        WebBook webBook = new WebBook(bookSource);
                        if (emitter != null && !emitter.isDisposed())
                            webBook.exploreBook(url, page, new CallBack<List<SearchBookBean>>() {
                                @Override
                                public void callBack(List<SearchBookBean> searchBookBeans) {
                                    emitter.onNext(searchBookBeans);
                                    emitter.onComplete();
                                }
                            });
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                }
            });
        } else {
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
        //判断源的版本
        if (bookSource.getFrom() == Consts.YUEDU_2_0) {
            return search2(content, page);
        } else if (bookSource.getFrom() == YUEDU_3_0) {
            return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
                @Override
                public void subscribe(final ObservableEmitter<List<SearchBookBean>> emitter) throws Exception {
                    int p = page + 1;
                    WebBook webBook = new WebBook(bookSource);
                    if (retryModel == SEARCH_MODEL_LOADMORE) {
                        p++;
                    }
                    if (emitter != null && !emitter.isDisposed())
                        webBook.searchBook(content, p, new CallBack<List<SearchBookBean>>() {
                            @Override
                            public void callBack(List<SearchBookBean> searchBookBeans) {
                                Loger.H("(" + bookSource.getBookSourceName() + ")当前线程：" + Thread.currentThread().getName());
                                emitter.onNext(searchBookBeans);
                                emitter.onComplete();
                            }
                        });
                }
            });
        } else {
            return search2(content, page);
        }
    }

    private Observable<List<SearchBookBean>> search2(final String content, final int page) {
        final BookList bookList = new BookList(tag, name, bookSource, false);
        try {
            return Observable.just(page)
                    .map(new Function<Integer, AnalyzeUrl>() {
                        @Override
                        public AnalyzeUrl apply(Integer integer) throws Exception {
                            int p = page;
                            if (retryModel == SEARCH_MODEL_DEFAULT) {
                                p = page;
                            } else if (retryModel == SEARCH_MODEL_LOADMORE) {
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
        //区分版本
        if (bookSource.getFrom() == YUEDU_3_0) {
            return Observable.create(new ObservableOnSubscribe<BookShelfBean>() {
                @Override
                public void subscribe(final ObservableEmitter<BookShelfBean> emitter) throws Exception {
                    try {
                        WebBook webBook = new WebBook(bookSource);
                        if (emitter != null && !emitter.isDisposed())
                            webBook.getBookInfo(bookShelfBean.getBookInfoBean(), new CallBack<BookInfoBean>() {
                                @Override
                                public void callBack(BookInfoBean bookInfoBean) {
                                    bookShelfBean.setBookInfoBean(bookInfoBean);
                                    emitter.onNext(bookShelfBean);
                                    emitter.onComplete();
                                }
                            });
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                }
            });
        } else {
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

    }


    /**
     * 获取目录
     */
    public Observable<List<BookChapterBean>> getChapterList(final BookShelfBean bookShelfBean) {
        if (bookSource == null) {
            return Observable.error(new Throwable("书源错误"));
        }
        //区分版本
        if (bookSource.getFrom() == YUEDU_3_0) {
            return Observable.create(new ObservableOnSubscribe<List<BookChapterBean>>() {
                @Override
                public void subscribe(final ObservableEmitter<List<BookChapterBean>> emitter) throws Exception {
                    try {
                        WebBook webBook = new WebBook(bookSource);
                        if (emitter != null && !emitter.isDisposed())
                            webBook.getChapterList(bookShelfBean.getBookInfoBean(), new CallBack<List<BookChapterBean>>() {
                                @Override
                                public void callBack(List<BookChapterBean> bookChapterBeans) {
                                    emitter.onNext(bookChapterBeans);
                                    emitter.onComplete();
                                }
                            });
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                }
            });
        } else {
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
                    bookContentBean.setDurChapterContent(chapterBean.getUrl());
                    bookContentBean.setDurChapterIndex(chapterBean.getIndex());
                    bookContentBean.setTag(bookShelfBean.getTag());
                    bookContentBean.setDurChapterUrl(chapterBean.getUrl());
                    emitter.onNext(bookContentBean);
                    emitter.onComplete();
                }
            });
        }

        //区分版本
        if (bookSource.getFrom() == YUEDU_3_0) {
            return Observable.create(new ObservableOnSubscribe<BookContentBean>() {
                @Override
                public void subscribe(final ObservableEmitter<BookContentBean> emitter) throws Exception {
                    try {
                        WebBook webBook = new WebBook(bookSource);
                        String nextUrl = nextChapterBean != null ? nextChapterBean.getUrl() : "";
                        if (emitter != null && !emitter.isDisposed())
                            webBook.getContent(bookShelfBean.getBookInfoBean(), chapterBean, nextUrl, isTest, new CallBack<String>() {
                                @Override
                                public void callBack(String s) {
                                    BookContentBean bookContentBean = new BookContentBean();
                                    bookContentBean.setDurChapterContent(s);
                                    bookContentBean.setDurChapterUrl(chapterBean.getUrl());
                                    emitter.onNext(bookContentBean);
                                    emitter.onComplete();
                                }
                            });
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                }
            });
        } else {
            final BookContent bookContent = new BookContent(tag, bookSource);
            if (Objects.equals(chapterBean.getUrl(), bookShelfBean.getBookInfoBean().getChapterUrl())
                    && !TextUtils.isEmpty(bookShelfBean.getBookInfoBean().getChapterListHtml())) {
                return bookContent.analyzeBookContent(bookShelfBean.getBookInfoBean().getChapterListHtml(), chapterBean, nextChapterBean, bookShelfBean, headerMap, isTest);
            }
            try {
                AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapterBean.getUrl(), headerMap, bookShelfBean.getBookInfoBean().getChapterUrl());
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
                return Observable.error(new Throwable(String.format("url错误: %s", chapterBean.getUrl())));
            }
        }


    }


    public BookShelfBean bookShelf;
    public long startTime;
}
