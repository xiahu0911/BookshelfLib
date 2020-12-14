package com.flyersoft.source.dao;

import com.flyersoft.source.bean.Cache;
import com.flyersort.source.gen.CacheDao;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created By huzheng
 * Date 2020/4/30
 * Des 源初始化/增/删/改/查
 */
public class CacheController {

    private static CacheController cacheController;

    private CacheController() {
    }

    public static CacheController getInstance() {
        if (cacheController == null) {
            synchronized (CacheController.class) {
                if (cacheController == null) {
                    cacheController = new CacheController();
                }
            }
        }
        return cacheController;
    }

    public void insert(@NotNull Cache cache) {
        DaoController.getInstance().cacheDao.insertOrReplace(cache);
    }

    @Nullable
    public String get(@NotNull String key, Long now) {
        Long z = 0L;
        List<Cache> list = DaoController.getInstance().cacheDao.queryBuilder().where(CacheDao.Properties.Key.eq(key)).whereOr(CacheDao.Properties.Deadline.eq(z),
                CacheDao.Properties.Deadline.gt(now)).build().list();
        if (list != null && list.size() > 0) {
            return list.get(0).getValue();
        } else {
            return "";
        }
    }
}
