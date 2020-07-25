package com.flyersoft.source.manager.content;

import android.text.TextUtils;

import com.flyersoft.source.bean.BookInfoBean;
import com.flyersoft.source.bean.BookShelfBean;
import com.flyersoft.source.bean.BookSource;
import com.flyersoft.source.manager.BaseModel;
import com.flyersoft.source.manager.analyzeRule.AnalyzeByRegex;
import com.flyersoft.source.manager.analyzeRule.AnalyzeRule;
import com.flyersoft.source.utils.Loger;
import com.flyersoft.source.utils.StringUtils;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static android.text.TextUtils.isEmpty;

public class BookInfo extends BaseModel {
    private String tag;
    private String sourceName;
    private BookSource bookSourceBean;

    public BookInfo(String tag, String sourceName, BookSource bookSourceBean) {
        this.tag = tag;
        this.sourceName = sourceName;
        this.bookSourceBean = bookSourceBean;
    }

    public Observable<BookShelfBean> analyzeBookInfo(final String s, final BookShelfBean bookShelfBean) {
        return Observable.create(new ObservableOnSubscribe<BookShelfBean>() {
            @Override
            public void subscribe(ObservableEmitter<BookShelfBean> e) throws Exception {
                String baseUrl = bookShelfBean.getNoteUrl();

                if (TextUtils.isEmpty(s)) {
                    e.onError(new Throwable("详情-数据解析失败"));
                    return;
                } else {
                    Loger.showLog(tag, "┌成功获取详情页");
                    Loger.showLog(tag, "└" + baseUrl);
                }
                bookShelfBean.setTag(tag);

                BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
                bookInfoBean.setNoteUrl(baseUrl);   //id
                bookInfoBean.setTag(tag);
                bookInfoBean.setOrigin(sourceName);
                bookInfoBean.setBookSourceType(bookSourceBean.getBookSourceType()); // 是否为有声读物

                AnalyzeRule analyzer = new AnalyzeRule(bookShelfBean);
                analyzer.setContent(s, baseUrl);

                // 获取详情页预处理规则
                String ruleInfoInit = bookSourceBean.getRuleBookInfoInit();
                boolean isRegex = false;
                if (!isEmpty(ruleInfoInit)) {
                    // 仅使用java正则表达式提取书籍详情
                    if (ruleInfoInit.startsWith(":")) {
                        isRegex = true;
                        ruleInfoInit = ruleInfoInit.substring(1);
                        Loger.showLog(tag, "┌详情信息预处理");
                        AnalyzeByRegex.getInfoOfRegex(s, ruleInfoInit.split("&&"), 0, bookShelfBean, analyzer, bookSourceBean, tag);
                    } else {
                        Object object = analyzer.getElement(ruleInfoInit);
                        if (object != null) {
                            analyzer.setContent(object);
                        }
                    }
                }
                if (!isRegex) {
                    Loger.showLog(tag, "┌详情信息预处理");
                    Object object = analyzer.getElement(ruleInfoInit);
                    if (object != null) analyzer.setContent(object);
                    Loger.showLog(tag, "└详情预处理完成");

                    Loger.showLog(tag, "┌获取书名");
                    String bookName = StringUtils.formatHtml(analyzer.getString(bookSourceBean.getRuleBookName()));
                    if (!isEmpty(bookName)) bookInfoBean.setName(bookName);
                    Loger.showLog(tag, "└" + bookName);

                    Loger.showLog(tag, "┌获取作者");
                    String bookAuthor = StringUtils.formatHtml(analyzer.getString(bookSourceBean.getRuleBookAuthor()));
                    if (!isEmpty(bookAuthor)) bookInfoBean.setAuthor(bookAuthor);
                    Loger.showLog(tag, "└" + bookAuthor);

                    Loger.showLog(tag, "┌获取分类");
                    String bookKind = analyzer.getString(bookSourceBean.getRuleBookKind());
                    Loger.showLog(tag, "└" + bookKind);

                    Loger.showLog(tag, "┌获取最新章节");
                    String bookLastChapter = analyzer.getString(bookSourceBean.getRuleBookLastChapter());
                    if (!isEmpty(bookLastChapter))
                        bookShelfBean.setLastChapterName(bookLastChapter);
                    Loger.showLog(tag, "└" + bookLastChapter);

                    Loger.showLog(tag, "┌获取简介");
                    String bookIntroduce = analyzer.getString(bookSourceBean.getRuleIntroduce());
                    if (!isEmpty(bookIntroduce)) bookInfoBean.setIntroduce(bookIntroduce);
                    Loger.showLog(tag, "└" + bookIntroduce);

                    Loger.showLog(tag, "┌获取封面");
                    String bookCoverUrl = analyzer.getString(bookSourceBean.getRuleCoverUrl(), true);
                    if (!isEmpty(bookCoverUrl)) bookInfoBean.setCoverUrl(bookCoverUrl);
                    Loger.showLog(tag, "└" + bookCoverUrl);

                    Loger.showLog(tag, "┌获取目录网址");
                    String bookCatalogUrl = analyzer.getString(bookSourceBean.getRuleChapterUrl(), true);
                    if (isEmpty(bookCatalogUrl)) bookCatalogUrl = baseUrl;
                    bookInfoBean.setChapterUrl(bookCatalogUrl);
                    //如果目录页和详情页相同,暂存页面内容供获取目录用
                    if (bookCatalogUrl.equals(baseUrl)) bookInfoBean.setChapterListHtml(s);
                    Loger.showLog(tag, "└" + bookInfoBean.getChapterUrl());
                    bookShelfBean.setBookInfoBean(bookInfoBean);
                    Loger.showLog(tag, "-详情页解析完成");
                }
                e.onNext(bookShelfBean);
                e.onComplete();
            }
        });
    }

}
