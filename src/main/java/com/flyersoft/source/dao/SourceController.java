package com.flyersoft.source.dao;

import com.flyersoft.source.bean.BookSource;
import com.flyersort.source.gen.BookSourceDao;

import java.util.List;

/**
 * Created By huzheng
 * Date 2020/4/30
 * Des 源初始化/增/删/改/查
 */
public class SourceController {

    private static SourceController sourceController;

    private SourceController() {
    }

    public static SourceController getInstance() {
        if (sourceController == null) {
            synchronized (SourceController.class) {
                if (sourceController == null) {
                    sourceController = new SourceController();
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
    public List<BookSource> getAll() {
        return DaoController.getInstance().bookSourceDao.queryBuilder()
                .orderAsc(BookSourceDao.Properties.Weight).list();
    }

    /**
     * 获取所有选中源（按更新时间排序）
     *
     * @return
     */
    public List<BookSource> getAllSelect() {
        return DaoController.getInstance().bookSourceDao.queryBuilder()
                .where(BookSourceDao.Properties.Enable.eq(true))
                .orderAsc(BookSourceDao.Properties.Weight).list();
    }

    /**
     * 获取group
     *
     * @return
     */
    public List<BookSource> getAllByGroup(String group) {
        return DaoController.getInstance().bookSourceDao.queryBuilder()
                .where(BookSourceDao.Properties.BookSourceGroup.eq(group))
                .orderAsc(BookSourceDao.Properties.Weight).list();
    }

    /**
     * 批量插入/更新
     * @param bookSources
     */
    public void insertOrReplace(List<BookSource> bookSources) {
        if (bookSources == null || bookSources.size() <= 0) {
            return;
        }
        DaoController.getInstance().bookSourceDao.insertOrReplaceInTx(bookSources);
    }

    /**
     * 更新单个源
     * @param bookSources
     */
    public void insertOrReplace(BookSource bookSources) {
        if (bookSources == null) {
            return;
        }
        DaoController.getInstance().bookSourceDao.insertOrReplace(bookSources);
    }

    /**
     * 删除某个源
     * @param bookSources
     */
    public void deleteSource(BookSource bookSources) {
        if (bookSources == null) {
            return;
        }
        DaoController.getInstance().bookSourceDao.delete(bookSources);
    }

    /**
     * 批量删除
     * @param bookSources
     */
    public void deleteSource(List<BookSource> bookSources) {
        if (bookSources == null) {
            return;
        }
        DaoController.getInstance().bookSourceDao.deleteInTx(bookSources);
    }

    /**
     * 根据url进行查找
     * @param url
     * @return
     */
    public BookSource getBookSourceByUrl(String url) {
        return DaoController.getInstance().bookSourceDao.load(url);
    }


}
