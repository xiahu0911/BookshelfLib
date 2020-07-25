package com.flyersoft.source.dao;

import com.flyersoft.source.bean.CookieBean;

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
    public CookieBean getCookie(String url) {
        return DaoController.getInstance().cookieBeanDao.load(url);
    }

    public static void insertOrReplace(CookieBean cookieBean) {

    }


}
