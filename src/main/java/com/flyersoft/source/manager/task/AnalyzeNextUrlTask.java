package com.flyersoft.source.manager.task;

import com.flyersoft.source.bean.BookChapterBean;
import com.flyersoft.source.bean.BookShelfBean;
import com.flyersoft.source.bean.WebChapterBean;
import com.flyersoft.source.manager.BaseModel;
import com.flyersoft.source.manager.analyzeRule.AnalyzeUrl;
import com.flyersoft.source.manager.content.BookChapterList;

import java.util.List;
import java.util.Map;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class AnalyzeNextUrlTask {
    private WebChapterBean webChapterBean;
    private Callback callback;
    private BookShelfBean bookShelfBean;
    private Map<String, String> headerMap;
    private BookChapterList bookChapterList;

    public AnalyzeNextUrlTask(BookChapterList bookChapterList, WebChapterBean webChapterBean, BookShelfBean bookShelfBean, Map<String, String> headerMap) {
        this.bookChapterList = bookChapterList;
        this.webChapterBean = webChapterBean;
        this.bookShelfBean = bookShelfBean;
        this.headerMap = headerMap;
    }

    public AnalyzeNextUrlTask setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public void analyzeUrl(AnalyzeUrl analyzeUrl) {
        BaseModel.getInstance().getResponseO(analyzeUrl)
                .flatMap(new Function<Response<String>, ObservableSource<List<BookChapterBean>>>() {
                    @Override
                    public ObservableSource<List<BookChapterBean>> apply(Response<String> stringResponse) throws Exception {
                        return bookChapterList.analyzeChapterList(stringResponse.body(), bookShelfBean, headerMap);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<List<BookChapterBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        callback.addDisposable(d);
                    }

                    @Override
                    public void onNext(List<BookChapterBean> bookChapterBeans) {
                        callback.analyzeFinish(webChapterBean, bookChapterBeans);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.onError(throwable);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public interface Callback {
        void addDisposable(Disposable disposable);

        void analyzeFinish(WebChapterBean bean, List<BookChapterBean> bookChapterBeans);

        void onError(Throwable throwable);
    }
}
