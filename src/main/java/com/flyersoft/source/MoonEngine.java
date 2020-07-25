package com.flyersoft.source;

import com.flyersoft.source.bean.BookChapterBean;
import com.flyersoft.source.bean.BookContentBean;
import com.flyersoft.source.bean.BookShelfBean;
import com.flyersoft.source.bean.BookSource;
import com.flyersoft.source.bean.DiscoveryBean;
import com.flyersoft.source.bean.SearchBookBean;
import com.flyersoft.source.manager.BookModel;
import com.flyersoft.source.manager.DiscoveryModel;
import com.flyersoft.source.manager.SourceModel2;
import com.flyersoft.source.manager.engine.SearchEngine;
import com.flyersoft.source.manager.engine.TestEngine;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created By huzheng
 * Date 2020/5/13
 * Des 总引擎
 * 1.源管理
 * 2.执行搜索
 * 3.执行内容检索
 * 4.执行下载
 */
public class MoonEngine {

    private static MoonEngine moonEngine;

    private MoonEngine() {
    }

    public synchronized static MoonEngine getInstance() {
        if (moonEngine == null) {
            moonEngine = new MoonEngine();
        }
        return moonEngine;
    }
    /**
     * 获取所有发现源，以及源所包含的分类
     *
     * @param discoveryListener
     */
    public void getDiscoveryList(DiscoveryModel.DiscoveryListener discoveryListener) {
        DiscoveryModel findModel = new DiscoveryModel();
        findModel.getData(discoveryListener, false);
    }

    /**
     * 获取某个源的发现
     *
     * @param sourceBean
     * @return
     */
    public DiscoveryBean getDiscoveryListByBookSource(BookSource sourceBean) {
        DiscoveryModel findModel = new DiscoveryModel();
        return findModel.convertDeiscovery(sourceBean);
    }

    /**
     * 获取选中源的发现源列表
     * @param discoveryListener
     */
    public void getSelectDiscoverySourceList(DiscoveryModel.DiscoveryListener discoveryListener) {
        DiscoveryModel discoveryModel = new DiscoveryModel();
        discoveryModel.getData(discoveryListener, false);
    }

    /**
     * 发现书籍
     *
     * @param tag
     * @param url
     * @param page
     * @return
     */
    public Observable<List<SearchBookBean>> getDiscoveryBookList(String tag, String url, int page) {
        BookModel instance = BookModel.getInstance(tag);
        return instance.findBook(url, page);
    }

    /**
     * 搜索关键字
     * Observable会多次调用，某个源有搜索结果就会立即返回
     *
     * @param keyStr
     * @return
     */
    public void search(final String keyStr, final SearchEngine.SearchEngineListener searchEngineListener) {
        SearchEngine searchEngine = new SearchEngine(6);
        searchEngine.search(keyStr, 0, searchEngineListener);
    }

    /**
     * 获取书籍信息
     *
     * @param bookShelfBean
     * @return
     */
    public Observable<BookShelfBean> getBookInfo(BookShelfBean bookShelfBean) {
        BookModel instance = BookModel.getInstance(bookShelfBean.getTag());
        return instance.getInfo(bookShelfBean);
    }

    /**
     * 获取章节列表
     *
     * @param bookShelfBean
     * @return
     */
    public Observable<List<BookChapterBean>> getBookChapter(BookShelfBean bookShelfBean) {
        BookModel instance = BookModel.getInstance(bookShelfBean.getTag());
        return instance.getChapterList(bookShelfBean);
    }

    /**
     * 获取内容
     *
     * @param bookShelfBean
     * @return
     */
    public Observable<BookContentBean> getContent(BookShelfBean bookShelfBean, BookChapterBean bookChapterBean, BookChapterBean nextChapterBean, boolean isTest) {
        BookModel instance = BookModel.getInstance(bookShelfBean.getTag());
        return instance.getContent(bookShelfBean, bookChapterBean, nextChapterBean, isTest);
    }


    /**
     * 网络/本地导入源
     *
     * @param url
     * @return
     */
    public Observable<List<BookSource>> addSource(String url) {
        return SourceModel2.importSource(url);
    }

    /**
     * 根据关键字，测试源响应速度
     *
     * @param key
     * @return
     */
    public void testSource(String key, List<BookSource> bookSources, TestEngine.TestEngineListener listener) {
        TestEngine testEngine = new TestEngine();
        if (bookSources != null) {
            testEngine.setBookSources(bookSources);
        }
        testEngine.test(key, listener);
    }

}
