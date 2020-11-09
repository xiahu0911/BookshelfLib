package com.flyersoft.source.dao;

import com.flyersoft.source.bean.CookieBean;
import com.flyersoft.source.yuedu3.Cookie;
import com.flyersort.source.gen.CookieBeanDao;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created By huzheng
 * Date 2020/4/30
 * Des 源初始化/增/删/改/查
 */
public class CookieController {

    private static CookieController sourceController;

    private CookieController() {
    }

    public static CookieController getInstance() {
        if (sourceController == null) {
            synchronized (CookieController.class) {
                if (sourceController == null) {
                    sourceController = new CookieController();
                }
            }
        }
        return sourceController;
    }

    /**
     * 获取所有源（按更新时间排序）
     *
     * @return
     */
    public static CookieBean getCookie(String url) {
        return DaoController.getInstance().cookieBeanDao.load(url);
    }

    public static void insertOrReplace(CookieBean cookieBean) {
        DaoController.getInstance().cookieBeanDao.insertOrReplace(cookieBean);
    }

    public static void insertOrReplace(CookieBean... cookieBeans) {
        DaoController.getInstance().cookieBeanDao.insertOrReplaceInTx(cookieBeans);
    }

    public static void del(String url) {
        DaoController.getInstance().cookieBeanDao.deleteByKey(url);
    }

    public static List<CookieBean> getOkHttpCookies() {
        return DaoController.getInstance().cookieBeanDao.queryBuilder().where(CookieBeanDao.Properties.Url.like("%|%")).build().list();
    }


    public static void deleteOkHttp() {
        DaoController.getInstance().cookieBeanDao.queryBuilder().where(CookieBeanDao.Properties.Url.like("%|%")).buildDelete().executeDeleteWithoutDetachingEntities();
    }
}
