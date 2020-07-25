package com.flyersoft.source.manager;

import com.flyersoft.source.base.IHttpGetApi;
import com.flyersoft.source.bean.BookSource;
import com.flyersoft.source.manager.analyzeRule.AnalyzeHeaders;
import com.flyersoft.source.utils.JsonUtils;
import com.flyersoft.source.utils.Loger;
import com.flyersoft.source.utils.NetworkUtils;
import com.flyersoft.source.utils.RxUtils;
import com.flyersoft.source.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import retrofit2.Response;

/**
 * Created By huzheng
 * Date 2020/5/7
 * Des 书籍爬虫
 */
public class SourceModel extends BaseModel {

    public static Observable<List<BookSource>> importSource(String string) {
        if (StringUtils.isTrimEmpty(string)) return null;
        string = string.trim();
        if (NetworkUtils.isIPv4Address(string)) {
            string = String.format("http://%s:65501", string);
        }
        if (StringUtils.isJsonType(string)) {
            return importBookSourceFromJson(string.trim())
                    .compose(new ObservableTransformer<List<BookSource>, List<BookSource>>() {
                        @Override
                        public ObservableSource<List<BookSource>> apply(Observable<List<BookSource>> upstream) {
                            return RxUtils.toSimpleSingle(upstream);
                        }
                    });
        }
        if (NetworkUtils.isUrl(string)) {
            return BaseModel.getInstance().getRetrofitString(StringUtils.getBaseUrl(string), "utf-8")
                    .create(IHttpGetApi.class)
                    .get(string, AnalyzeHeaders.getMap(null))
                    .flatMap(new Function<Response<String>, ObservableSource<List<BookSource>>>() {
                        @Override
                        public ObservableSource<List<BookSource>> apply(Response<String> stringResponse) throws Exception {
                            return importBookSourceFromJson(stringResponse.body());
                        }
                    })
                    .map(new Function<List<BookSource>, List<BookSource>>() {
                        @Override
                        public List<BookSource> apply(List<BookSource> bookSources) throws Exception {
                            for (BookSource bookSource : bookSources) {
                                bookSource.setBookSourceName(keepOnlyFilenameChars(bookSource.getBookSourceName()));
                            }
                            return bookSources;
                        }
                    })
                    .compose(new ObservableTransformer<List<BookSource>, List<BookSource>>() {
                        @Override
                        public ObservableSource<List<BookSource>> apply(Observable<List<BookSource>> upstream) {
                            return RxUtils.toSimpleSingle(upstream);
                        }
                    });
        }
        return Observable.error(new Throwable("不是Json或Url格式"));
    }


    private static Observable<List<BookSource>> importBookSourceFromJson(final String json) {
        return Observable.create(new ObservableOnSubscribe<List<BookSource>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BookSource>> e) throws Exception {
                List<BookSource> bookSourceBeans = new ArrayList<>();
                if (StringUtils.isJsonArray(json)) {
                    try {
                        bookSourceBeans = JsonUtils.gsonToList(json, BookSource.class);
                        for (BookSource bookSource : bookSourceBeans) {
                            bookSource.setBookSourceName(keepOnlyFilenameChars(bookSource.getBookSourceName()));
                        }
                        e.onNext(bookSourceBeans);
                        e.onComplete();
                        return;
                    } catch (Exception ignored) {
                    }
                }
                if (StringUtils.isJsonObject(json)) {
                    try {
                        BookSource bookSourceBean = JsonUtils.gsonToObject(json, BookSource.class);
//                        addBookSource(bookSourceBean);
                        bookSourceBeans.add(bookSourceBean);
                        for (BookSource bookSource : bookSourceBeans) {
                            bookSource.setBookSourceName(keepOnlyFilenameChars(bookSource.getBookSourceName()));
                            if (bookSource.getBookSourceUrl().endsWith("/")) {
                                bookSource.setBookSourceUrl(bookSource.getBookSourceUrl().replaceAll("/+$", ""));
                            }
                        }
                        e.onNext(bookSourceBeans);
                        e.onComplete();
                        return;
                    } catch (Exception ignored) {
                    }
                }
                e.onError(new Throwable("格式不对"));
            }
        });
    }


    public static void addBookSource(BookSource bookSourceBean) {
        Loger.H("添加到本地：" + bookSourceBean.getBookSourceName());
//        if (TextUtils.isEmpty(bookSourceBean.getBookSourceName()) || TextUtils.isEmpty(bookSourceBean.getBookSourceUrl()))
//            return;
//        if (bookSourceBean.getBookSourceUrl().endsWith("/")) {
//            bookSourceBean.setBookSourceUrl(bookSourceBean.getBookSourceUrl().replaceAll("/+$", ""));
//        }
//        BookSource temp = DbHelper.getDaoSession().getBookSourceBeanDao().queryBuilder()
//                .where(BookSourceBeanDao.Properties.BookSourceUrl.eq(bookSourceBean.getBookSourceUrl())).unique();
//        if (temp != null) {
//            bookSourceBean.setSerialNumber(temp.getSerialNumber());
//        }
//        if (bookSourceBean.getSerialNumber() < 0) {
//            bookSourceBean.setSerialNumber((int) (DbHelper.getDaoSession().getBookSourceBeanDao().queryBuilder().count() + 1));
//        }
//        DbHelper.getDaoSession().getBookSourceBeanDao().insertOrReplace(bookSourceBean);
    }


    /**
     * 规范源名称
     * 方便本地存储
     *
     * @param str
     * @return
     */
    private static String keepOnlyFilenameChars(String str) {
        try {
            Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
            Matcher m = pattern.matcher(str);
            return m.replaceAll("").trim().replaceAll("\n", "").replaceAll("#", "");
        } catch (Exception e) {
            return str;
        }
    }

}
