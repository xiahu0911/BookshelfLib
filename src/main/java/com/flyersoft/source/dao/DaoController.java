package com.flyersoft.source.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.flyersoft.source.bean.BookSource;
import com.flyersoft.source.utils.Loger;
import com.flyersoft.source.utils.Toaster;
import com.flyersort.source.gen.BookSourceDao;
import com.flyersort.source.gen.CacheDao;
import com.flyersort.source.gen.CookieBeanDao;
import com.flyersort.source.gen.DaoMaster;
import com.flyersort.source.gen.DaoSession;
import com.flyersort.source.gen.HttpTTSDao;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.internal.DaoConfig;

import java.io.InputStream;

/**
 * Created By huzheng
 * Date 2020/4/30
 * Des 数据库初始化
 */
public class DaoController {

    private static DaoController daoController;
    private DaoMaster daoMaster;
    private DaoSession daoSession;

    public BookSourceDao bookSourceDao;
    public CookieBeanDao cookieBeanDao;
    public CacheDao cacheDao;
    public HttpTTSDao httpTTSDao;

    private DaoController() {
    }

    private DaoController(final Context context) {
        //根据版本升级数据库
//        DaoMaster.DevOpenHelper moon = new DaoMaster.DevOpenHelper(context, "moon-db") {
//            @Override
//            public void onUpgrade(Database db, int oldVersion, int newVersion) {
//                String basefolder = "sql";
//                StringBuffer sb_exesql;
//                InputStream inputStream;
//                int length = -1;
//
//                for (int i = oldVersion; i < newVersion; i++) {
//                    try {
//                        sb_exesql = new StringBuffer();
//                        length = -1;
//                        byte[] bts = new byte[1024 * 12];
//                        inputStream = context.getAssets().open(basefolder + "/" + (oldVersion + 1) + ".sql");
//                        while ((length = inputStream.read(bts)) != -1) {
//                            sb_exesql.append(new String(bts, 0, length));
//                        }
//                        String[] split = sb_exesql.toString().split("\n");
//                        for (String s : split) {
//                            db.execSQL(s);
//                        }
//                    } catch (Exception e) {
//                        Loger.H(e.getMessage());
//                        Toaster.showToastCenter(context, "数据异常，建议重新安装！");
//                    }
//                }
//            }
//        };

        // 注意：默认的DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        GreenDaoUpgradeHelper mHelper = new GreenDaoUpgradeHelper(context, "moon-db", null);
        SQLiteDatabase readableDatabase = mHelper.getWritableDatabase();
//        SQLiteDatabase readableDatabase = moon.getReadableDatabase();
        daoMaster = new DaoMaster(readableDatabase);
        daoSession = daoMaster.newSession();

        bookSourceDao = daoSession.getBookSourceDao();
        cookieBeanDao = daoSession.getCookieBeanDao();
        cacheDao = daoSession.getCacheDao();
        httpTTSDao = daoSession.getHttpTTSDao();
    }

    public static void init(Context context) {
        if (daoController == null) {
            synchronized (DaoController.class) {
                if (daoController == null) {
                    daoController = new DaoController(context);
                }
            }
        }
    }

    public static DaoController getInstance() {
        return daoController;
    }

}
