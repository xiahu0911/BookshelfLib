package com.flyersoft.source.manager.content;

import android.text.TextUtils;

import com.flyersoft.source.bean.BookChapterBean;
import com.flyersoft.source.bean.BookContentBean;
import com.flyersoft.source.bean.BookShelfBean;
import com.flyersoft.source.bean.BookSource;
import com.flyersoft.source.manager.BaseModel;
import com.flyersoft.source.manager.analyzeRule.AnalyzeRule;
import com.flyersoft.source.manager.analyzeRule.AnalyzeUrl;
import com.flyersoft.source.utils.Loger;
import com.flyersoft.source.utils.NetworkUtils;
import com.flyersoft.source.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import retrofit2.Response;

import static com.flyersoft.source.conf.Consts.JS_PATTERN;

public class BookContent {
    private String tag;
    private BookSource bookSourceBean;
    private String ruleBookContent;
    private String baseUrl;

    public BookContent(String tag, BookSource bookSourceBean) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;
        ruleBookContent = bookSourceBean.getRuleBookContent();
        if (ruleBookContent.startsWith("$") && !ruleBookContent.startsWith("$.")) {
            ruleBookContent = ruleBookContent.substring(1);
            Matcher jsMatcher = JS_PATTERN.matcher(ruleBookContent);
            if (jsMatcher.find()) {
                ruleBookContent = ruleBookContent.replace(jsMatcher.group(), "");
            }
        }
    }

    public Observable<BookContentBean> analyzeBookContent(final Response<String> response, final BookChapterBean chapterBean, final BookChapterBean nextChapterBean, BookShelfBean bookShelfBean, Map<String, String> headerMap, boolean isTest) {
        baseUrl = NetworkUtils.getUrl(response);
        return analyzeBookContent(response.body(), chapterBean, nextChapterBean, bookShelfBean, headerMap, isTest);
    }

    public Observable<BookContentBean> analyzeBookContent(final String s, final BookChapterBean chapterBean, final BookChapterBean nextChapterBean, final BookShelfBean bookShelfBean, final Map<String, String> headerMap, final boolean isTest) {
        return Observable.create(new ObservableOnSubscribe<BookContentBean>() {
            @Override
            public void subscribe(ObservableEmitter<BookContentBean> e) throws Exception {
                if (TextUtils.isEmpty(s)) {
                    e.onError(new Throwable("内容-获取失败"));
                    return;
                }
                if (TextUtils.isEmpty(baseUrl)) {
                    baseUrl = NetworkUtils.getAbsoluteURL(bookShelfBean.getBookInfoBean().getChapterUrl(), chapterBean.getDurChapterUrl());
                }
                Loger.showLog(tag, "┌成功获取正文页");
                Loger.showLog(tag, "└" + baseUrl);
                BookContentBean bookContentBean = new BookContentBean();
                bookContentBean.setDurChapterIndex(chapterBean.getDurChapterIndex());
                bookContentBean.setDurChapterUrl(chapterBean.getDurChapterUrl());
                bookContentBean.setTag(tag);
                AnalyzeRule analyzer = new AnalyzeRule(bookShelfBean);
                WebContentBean webContentBean = analyzeBookContent(analyzer, s, chapterBean.getDurChapterUrl(), baseUrl);
                bookContentBean.setDurChapterContent(webContentBean.content);

                /*
                 * 处理分页
                 */
                if (!TextUtils.isEmpty(webContentBean.nextUrl)) {
                    List<String> usedUrlList = new ArrayList<>();
                    usedUrlList.add(chapterBean.getDurChapterUrl());
                    BookChapterBean nextChapter = null;
                    if (nextChapterBean != null) {
                        nextChapter = nextChapterBean;
                    }
//                else {
//                    nextChapter = DbHelper.getDaoSession().getBookChapterBeanDao().queryBuilder()
//                            .where(BookChapterBeanDao.Properties.NoteUrl.eq(chapterBean.getNoteUrl()),
//                                    BookChapterBeanDao.Properties.DurChapterIndex.eq(chapterBean.getDurChapterIndex() + 1))
//                            .build().unique();
//                }


                    while (!TextUtils.isEmpty(webContentBean.nextUrl) && !usedUrlList.contains(webContentBean.nextUrl)) {
                        usedUrlList.add(webContentBean.nextUrl);
                        if (nextChapter != null && NetworkUtils.getAbsoluteURL(baseUrl, webContentBean.nextUrl).equals(NetworkUtils.getAbsoluteURL(baseUrl, nextChapter.getDurChapterUrl()))) {
                            break;
                        }
                        if (isTest) {
                            break;
                        }
                        AnalyzeUrl analyzeUrl = new AnalyzeUrl(webContentBean.nextUrl, headerMap, tag);
                        try {
                            String body;
                            Response<String> response = BaseModel.getInstance().getResponseO(analyzeUrl).blockingFirst();
                            body = response.body();
                            webContentBean = analyzeBookContent(analyzer, body, webContentBean.nextUrl, baseUrl);
                            if (!TextUtils.isEmpty(webContentBean.content)) {
                                bookContentBean.setDurChapterContent(bookContentBean.getDurChapterContent() + "\n" + webContentBean.content);
                            }
                        } catch (Throwable exception) {
                            if (!e.isDisposed()) {
                                e.onError(exception);
                            }
                        }
                    }
                }
                e.onNext(bookContentBean);
                e.onComplete();
            }
        });
    }

    private WebContentBean analyzeBookContent(AnalyzeRule analyzer, final String s, final String chapterUrl, String baseUrl) throws Exception {
        WebContentBean webContentBean = new WebContentBean();

        analyzer.setContent(s, NetworkUtils.getAbsoluteURL(baseUrl, chapterUrl));
        Loger.showLog(tag, "┌解析正文内容");
        if (ruleBookContent.equals("all") || ruleBookContent.contains("@all")) {
            webContentBean.content = analyzer.getString(ruleBookContent);
        } else {
            webContentBean.content = StringUtils.formatHtml(analyzer.getString(ruleBookContent));
        }
        Loger.showLog(tag, "└" + webContentBean.content);
        String nextUrlRule = bookSourceBean.getRuleContentUrlNext();
        if (!TextUtils.isEmpty(nextUrlRule)) {
            Loger.showLog(tag, "┌解析下一页url");
            webContentBean.nextUrl = analyzer.getString(nextUrlRule, true);
            Loger.showLog(tag, "└" + webContentBean.nextUrl);
        }

        return webContentBean;
    }

    private class WebContentBean {
        private String content;
        private String nextUrl;

        private WebContentBean() {

        }
    }
}
