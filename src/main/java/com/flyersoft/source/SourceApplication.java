package com.flyersoft.source;

import android.app.Application;

import com.flyersoft.source.conf.Keys;
import com.flyersoft.source.dao.DaoController;
import com.flyersoft.source.utils.Loger;
import com.flyersoft.source.utils.SPUtils;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Created By huzheng
 * Date 2020/4/30
 * Des
 */
public class SourceApplication {

    public static Application INSTANCE;


    public static void init(Application application) {
        SourceApplication.INSTANCE = application;
        //初始化数据库
        DaoController.init(application);
        //sp本地存储
        SPUtils.init(application, Keys.SP_KEY);
        //rxjava异常接收
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
                Loger.showLog("RxjavaException", throwable.getMessage());
            }
        });
    }
}
