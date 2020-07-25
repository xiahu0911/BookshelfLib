package com.flyersoft.source.utils;

import com.flyersoft.source.bean.BookInfoBean;
import com.flyersoft.source.bean.BookShelfBean;
import com.flyersoft.source.bean.SearchBookBean;

/**
 * Created By huzheng
 * Date 2020/6/2
 * Des
 */
public class BookUtils {

    /**
     * 搜索转书籍
     */
    public static BookShelfBean getBookFromSearchBook(SearchBookBean searchBookBean) {
        BookShelfBean bookShelfBean = new BookShelfBean();
        bookShelfBean.setTag(searchBookBean.getTag());
        bookShelfBean.setNoteUrl(searchBookBean.getNoteUrl());
        bookShelfBean.setFinalDate(System.currentTimeMillis());
        bookShelfBean.setDurChapter(0);
        bookShelfBean.setDurChapterPage(0);
        bookShelfBean.setVariable(searchBookBean.getVariable());
        BookInfoBean bookInfo = bookShelfBean.getBookInfoBean();
        bookInfo.setNoteUrl(searchBookBean.getNoteUrl());
        bookInfo.setAuthor(searchBookBean.getAuthor());
        bookInfo.setCoverUrl(searchBookBean.getCoverUrl());
        bookInfo.setName(searchBookBean.getName());
        bookInfo.setTag(searchBookBean.getTag());
        bookInfo.setOrigin(searchBookBean.getOrigin());
        bookInfo.setIntroduce(searchBookBean.getIntroduce());
        bookInfo.setChapterUrl(searchBookBean.getChapterUrl());
        bookInfo.setBookInfoHtml(searchBookBean.getBookInfoHtml());
        bookShelfBean.setVariable(searchBookBean.getVariable());
        return bookShelfBean;
    }
}
