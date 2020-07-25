package com.flyersoft.source.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.flyersoft.source.utils.Loger;
import com.flyersoft.source.utils.Toaster;
import com.flyersort.source.gen.BookSourceDao;
import com.flyersort.source.gen.CookieBeanDao;
import com.flyersort.source.gen.DaoMaster;
import com.flyersort.source.gen.DaoSession;

import org.greenrobot.greendao.database.Database;

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

    private DaoController() {
    }

    private DaoController(final Context context) {
        //根据版本升级数据库
        DaoMaster.DevOpenHelper moon = new DaoMaster.DevOpenHelper(context, "moon-db") {
            @Override
            public void onUpgrade(Database db, int oldVersion, int newVersion) {
                String basefolder = "sql";
                StringBuffer sb_exesql;
                InputStream inputStream;
                int length = -1;
                for (int i = oldVersion; i < newVersion; i++) {
                    try {
                        sb_exesql = new StringBuffer();
                        length = -1;
                        byte[] bts = new byte[1024 * 12];
                        inputStream = context.getAssets().open(basefolder + "/" + (oldVersion + 1) + ".sql");
                        while ((length = inputStream.read(bts)) != -1) {
                            sb_exesql.append(new String(bts, 0, length));
                        }
                        db.execSQL(sb_exesql.toString());
                    } catch (Exception e) {
                        Loger.H(e.getMessage());
                        Toaster.showToastCenter(context, "数据异常，建议重新安装！");
                    }
                }
            }
        };
        SQLiteDatabase readableDatabase = moon.getReadableDatabase();
        daoMaster = new DaoMaster(readableDatabase);
        daoSession = daoMaster.newSession();

        bookSourceDao = daoSession.getBookSourceDao();
        cookieBeanDao = daoSession.getCookieBeanDao();
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
