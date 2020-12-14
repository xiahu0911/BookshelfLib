package com.flyersort.source.gen;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.flyersoft.source.bean.BookChapterBean;
import com.flyersoft.source.bean.BookContentBean;
import com.flyersoft.source.bean.BookShelfBean;
import com.flyersoft.source.bean.BookSource;
import com.flyersoft.source.bean.Cache;
import com.flyersoft.source.bean.CookieBean;
import com.flyersoft.source.bean.HttpTTS;

import com.flyersort.source.gen.BookChapterBeanDao;
import com.flyersort.source.gen.BookContentBeanDao;
import com.flyersort.source.gen.BookShelfBeanDao;
import com.flyersort.source.gen.BookSourceDao;
import com.flyersort.source.gen.CacheDao;
import com.flyersort.source.gen.CookieBeanDao;
import com.flyersort.source.gen.HttpTTSDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig bookChapterBeanDaoConfig;
    private final DaoConfig bookContentBeanDaoConfig;
    private final DaoConfig bookShelfBeanDaoConfig;
    private final DaoConfig bookSourceDaoConfig;
    private final DaoConfig cacheDaoConfig;
    private final DaoConfig cookieBeanDaoConfig;
    private final DaoConfig httpTTSDaoConfig;

    private final BookChapterBeanDao bookChapterBeanDao;
    private final BookContentBeanDao bookContentBeanDao;
    private final BookShelfBeanDao bookShelfBeanDao;
    private final BookSourceDao bookSourceDao;
    private final CacheDao cacheDao;
    private final CookieBeanDao cookieBeanDao;
    private final HttpTTSDao httpTTSDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        bookChapterBeanDaoConfig = daoConfigMap.get(BookChapterBeanDao.class).clone();
        bookChapterBeanDaoConfig.initIdentityScope(type);

        bookContentBeanDaoConfig = daoConfigMap.get(BookContentBeanDao.class).clone();
        bookContentBeanDaoConfig.initIdentityScope(type);

        bookShelfBeanDaoConfig = daoConfigMap.get(BookShelfBeanDao.class).clone();
        bookShelfBeanDaoConfig.initIdentityScope(type);

        bookSourceDaoConfig = daoConfigMap.get(BookSourceDao.class).clone();
        bookSourceDaoConfig.initIdentityScope(type);

        cacheDaoConfig = daoConfigMap.get(CacheDao.class).clone();
        cacheDaoConfig.initIdentityScope(type);

        cookieBeanDaoConfig = daoConfigMap.get(CookieBeanDao.class).clone();
        cookieBeanDaoConfig.initIdentityScope(type);

        httpTTSDaoConfig = daoConfigMap.get(HttpTTSDao.class).clone();
        httpTTSDaoConfig.initIdentityScope(type);

        bookChapterBeanDao = new BookChapterBeanDao(bookChapterBeanDaoConfig, this);
        bookContentBeanDao = new BookContentBeanDao(bookContentBeanDaoConfig, this);
        bookShelfBeanDao = new BookShelfBeanDao(bookShelfBeanDaoConfig, this);
        bookSourceDao = new BookSourceDao(bookSourceDaoConfig, this);
        cacheDao = new CacheDao(cacheDaoConfig, this);
        cookieBeanDao = new CookieBeanDao(cookieBeanDaoConfig, this);
        httpTTSDao = new HttpTTSDao(httpTTSDaoConfig, this);

        registerDao(BookChapterBean.class, bookChapterBeanDao);
        registerDao(BookContentBean.class, bookContentBeanDao);
        registerDao(BookShelfBean.class, bookShelfBeanDao);
        registerDao(BookSource.class, bookSourceDao);
        registerDao(Cache.class, cacheDao);
        registerDao(CookieBean.class, cookieBeanDao);
        registerDao(HttpTTS.class, httpTTSDao);
    }
    
    public void clear() {
        bookChapterBeanDaoConfig.clearIdentityScope();
        bookContentBeanDaoConfig.clearIdentityScope();
        bookShelfBeanDaoConfig.clearIdentityScope();
        bookSourceDaoConfig.clearIdentityScope();
        cacheDaoConfig.clearIdentityScope();
        cookieBeanDaoConfig.clearIdentityScope();
        httpTTSDaoConfig.clearIdentityScope();
    }

    public BookChapterBeanDao getBookChapterBeanDao() {
        return bookChapterBeanDao;
    }

    public BookContentBeanDao getBookContentBeanDao() {
        return bookContentBeanDao;
    }

    public BookShelfBeanDao getBookShelfBeanDao() {
        return bookShelfBeanDao;
    }

    public BookSourceDao getBookSourceDao() {
        return bookSourceDao;
    }

    public CacheDao getCacheDao() {
        return cacheDao;
    }

    public CookieBeanDao getCookieBeanDao() {
        return cookieBeanDao;
    }

    public HttpTTSDao getHttpTTSDao() {
        return httpTTSDao;
    }

}
