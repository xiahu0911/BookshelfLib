package com.flyersoft.source.manager.engine;

import com.flyersoft.source.bean.BookShelfBean;
import com.flyersoft.source.bean.BookSource;
import com.flyersoft.source.dao.SourceController;
import com.flyersoft.source.manager.BookModel;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created By huzheng
 * Date 2020/5/13
 * Des 搜索引擎
 */
public class BookEngine {

    private BookSource bookSource;
    private CompositeDisposable compositeDisposable;//网络请求管理

    public BookEngine(BookShelfBean bookShelfBean) {
        compositeDisposable = new CompositeDisposable();
        bookSource = SourceController.getInstance().getBookSourceByUrl(bookShelfBean.getTag());
    }

    public synchronized Observable<BookShelfBean> getInfo(BookShelfBean bookShelfBean) {
        BookModel bookModel = BookModel.getInstance(bookSource.getBookSourceUrl());
        return bookModel.getInfo(bookShelfBean);
    }
}
