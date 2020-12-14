package com.flyersoft.source.dao;

import com.flyersoft.source.bean.HttpTTS;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created By huzheng
 * Date 2020/4/30
 * Des 源初始化/增/删/改/查
 */
public class HttpTTSController {

    private static HttpTTSController cacheController;

    private HttpTTSController() {
    }

    public static HttpTTSController getInstance() {
        if (cacheController == null) {
            synchronized (HttpTTSController.class) {
                if (cacheController == null) {
                    cacheController = new HttpTTSController();
                }
            }
        }
        return cacheController;
    }

    public void init(List<HttpTTS> fromJsonArray) {
        List<HttpTTS> httpTTS = DaoController.getInstance().httpTTSDao.loadAll();
        if (httpTTS == null || httpTTS.size() == 0) {
            insert(fromJsonArray);
        }
    }

    public void insert(@NotNull List<HttpTTS> httpTTSs) {
        DaoController.getInstance().httpTTSDao.insertOrReplaceInTx(httpTTSs);
    }

    @Nullable
    public HttpTTS get(@NotNull Long id) {
        return DaoController.getInstance().httpTTSDao.load(id);
    }
}
