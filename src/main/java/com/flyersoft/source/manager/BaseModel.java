package com.flyersoft.source.manager;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.flyersoft.source.SourceApplication;
import com.flyersoft.source.base.EncodeConverter;
import com.flyersoft.source.base.IHttpGetApi;
import com.flyersoft.source.base.IHttpPostApi;
import com.flyersoft.source.base.SSLSocketClient;
import com.flyersoft.source.bean.CookieBean;
import com.flyersoft.source.dao.CookieController;
import com.flyersoft.source.manager.analyzeRule.AnalyzeUrl;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class BaseModel {

    public static final int SEARCH_MODEL_DEFAULT = 0;
    public static final int SEARCH_MODEL_LOADMORE = 1;
    public static final int SEARCH_MODEL_RELOAD = 2;

    private static OkHttpClient httpClient;

    public static BaseModel getInstance() {
        return new BaseModel();
    }

    public Observable<Response<String>> getResponseO(AnalyzeUrl analyzeUrl) {
        switch (analyzeUrl.getUrlMode()) {
            case POST:
                return getRetrofitString(analyzeUrl.getHost(), analyzeUrl.getCharCode())
                        .create(IHttpPostApi.class)
                        .postMap(analyzeUrl.getPath(),
                                analyzeUrl.getQueryMap(),
                                analyzeUrl.getHeaderMap());
            case GET:
                return getRetrofitString(analyzeUrl.getHost(), analyzeUrl.getCharCode())
                        .create(IHttpGetApi.class)
                        .getMap(analyzeUrl.getPath(),
                                analyzeUrl.getQueryMap(),
                                analyzeUrl.getHeaderMap());
            default:
                return getRetrofitString(analyzeUrl.getHost(), analyzeUrl.getCharCode())
                        .create(IHttpGetApi.class)
                        .get(analyzeUrl.getPath(),
                                analyzeUrl.getHeaderMap());
        }
    }

    public Retrofit getRetrofitString(String url) {
        return new Retrofit.Builder().baseUrl(url)
                //增加返回值为字符串的支持(以实体类返回)
                .addConverterFactory(EncodeConverter.create())
                //增加返回值为Observable<T>的支持
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getClient())
                .build();
    }

    public Retrofit getRetrofitString(String url, String encode) {
        return new Retrofit.Builder().baseUrl(url)
                //增加返回值为字符串的支持(以实体类返回)
                .addConverterFactory(EncodeConverter.create(encode))
                //增加返回值为Observable<T>的支持
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getClient())
                .build();
    }

    synchronized public static OkHttpClient getClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.createTrustAllManager())
                    .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                    .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                    .addInterceptor(getHeaderInterceptor())
                    .build();
        }
        return httpClient;
    }

    private static Interceptor getHeaderInterceptor() {
        return new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request request = chain.request()
                        .newBuilder()
                        .addHeader("Keep-Alive", "300")
                        .addHeader("Connection", "Keep-Alive")
                        .addHeader("Cache-Control", "no-cache")
                        .build();
                return chain.proceed(request);
            }
        };
    }

    protected Observable<Response<String>> setCookie(final Response<String> response, final String tag) {
        return Observable.create(new ObservableOnSubscribe<Response<String>>() {
            @Override
            public void subscribe(ObservableEmitter<Response<String>> e) throws Exception {
                if (!response.raw().headers("Set-Cookie").isEmpty()) {
                    StringBuilder cookieBuilder = new StringBuilder();
                    for (String s : response.raw().headers("Set-Cookie")) {
                        String[] x = s.split(";");
                        for (String y : x) {
                            if (!TextUtils.isEmpty(y)) {
                                cookieBuilder.append(y).append(";");
                            }
                        }
                    }
                    String cookie = cookieBuilder.toString();
                    if (!TextUtils.isEmpty(cookie)) {
                        CookieController.insertOrReplace(new CookieBean(tag, cookie));
                    }
                }
                e.onNext(response);
                e.onComplete();
            }
        });
    }

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    protected Observable<String> getAjaxString(final AnalyzeUrl analyzeUrl, final String tag, String js) {
        final Web web = new Web("加载超时");
        if (!TextUtils.isEmpty(js)) {
            web.js = js;
        }
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> e) throws Exception {
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        final Runnable r = this;
                        Runnable timeoutRunnable;
                        final WebView webView = new WebView(SourceApplication.INSTANCE);
                        webView.getSettings().setJavaScriptEnabled(true);
                        webView.getSettings().setUserAgentString(analyzeUrl.getHeaderMap().get("User-Agent"));
                        final CookieManager cookieManager = CookieManager.getInstance();
                        final Runnable retryRunnable = new Runnable() {
                            @Override
                            public void run() {
                                webView.evaluateJavascript(web.js, new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        if (!TextUtils.isEmpty(value)) {
                                            web.content = StringEscapeUtils.unescapeJson(value);
                                            e.onNext(web.content);
                                            e.onComplete();
                                            webView.destroy();
                                            handler.removeCallbacks(r);
                                        } else {
                                            handler.postDelayed(r, 1000);
                                        }
                                    }
                                });
                            }
                        };
                        timeoutRunnable = new Runnable() {
                            @Override
                            public void run() {
                                if (!e.isDisposed()) {
                                    handler.removeCallbacks(retryRunnable);
                                    e.onNext(web.content);
                                    e.onComplete();
                                    webView.destroy();
                                }
                            }
                        };
                        handler.postDelayed(timeoutRunnable, 30000);
                        webView.setWebViewClient(new WebViewClient() {
                            @Override
                            public void onPageFinished(WebView view, String url) {
                                CookieController.insertOrReplace(new CookieBean(tag, cookieManager.getCookie(webView.getUrl())));
                                handler.postDelayed(retryRunnable, 1000);
                            }
                        });
                        switch (analyzeUrl.getUrlMode()) {
                            case POST:
                                webView.postUrl(analyzeUrl.getUrl(), analyzeUrl.getPostData());
                                break;
                            case GET:
                                webView.loadUrl(String.format("%s?%s", analyzeUrl.getUrl(), analyzeUrl.getQueryStr()), analyzeUrl.getHeaderMap());
                                break;
                            default:
                                webView.loadUrl(analyzeUrl.getUrl(), analyzeUrl.getHeaderMap());
                        }
                    }
                });
            }
        });
    }

    private class Web {
        private String content;
        private String js = "document.documentElement.outerHTML";

        Web(String content) {
            this.content = content;
        }
    }

}