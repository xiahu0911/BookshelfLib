package com.flyersoft.source.manager.content;

import android.os.Build;
import android.text.TextUtils;

import com.flyersoft.source.bean.BookShelfBean;
import com.flyersoft.source.bean.BookSource;
import com.flyersoft.source.bean.SearchBookBean;
import com.flyersoft.source.manager.analyzeRule.AnalyzeByRegex;
import com.flyersoft.source.manager.analyzeRule.AnalyzeRule;
import com.flyersoft.source.utils.Loger;
import com.flyersoft.source.utils.NetworkUtils;
import com.flyersoft.source.utils.StringUtils;

import org.mozilla.javascript.NativeObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

public class BookList {

    public static final String SEARCH_ERR_NET = "搜索-访问网站失败";
    public static final String SEARCH_ERR_DATA = "搜索-数据解析失败";

    private String tag;
    private String sourceName;
    private BookSource bookSourceBean;
    private boolean isFind;
    //规则
    private String ruleList;
    private String ruleName;
    private String ruleAuthor;
    private String ruleKind;
    private String ruleIntroduce;
    private String ruleLastChapter;
    private String ruleCoverUrl;
    private String ruleNoteUrl;

    public BookList(String tag, String sourceName, BookSource bookSourceBean, boolean isFind) {
        this.tag = tag;
        this.sourceName = sourceName;
        this.bookSourceBean = bookSourceBean;
        this.isFind = isFind;
    }

    public Observable<List<SearchBookBean>> analyzeSearchBook(final Response<String> response) {

        return Observable.create(new ObservableOnSubscribe<List<SearchBookBean>>() {
            @Override
            public void subscribe(ObservableEmitter<List<SearchBookBean>> e) throws Exception {
                Loger.showLog(tag, sourceName + "--执行解析" + " - 【" + Thread.currentThread().getName() + "】");
                String baseUrl;
                baseUrl = NetworkUtils.getUrl(response);
                if (TextUtils.isEmpty(response.body())) {
                    e.onError(new Throwable(SEARCH_ERR_DATA));
                    return;
                } else {
                    Loger.showLog(tag, "┌成功获取搜索结果");
                    Loger.showLog(tag, "└" + baseUrl);
                }
                String body = response.body();
                List<SearchBookBean> books = new ArrayList<>();
                AnalyzeRule analyzer = new AnalyzeRule(null);
                analyzer.setContent(body, baseUrl);
                //如果符合详情页url规则
                if (!isEmpty(bookSourceBean.getRuleBookUrlPattern())
                        && baseUrl.matches(bookSourceBean.getRuleBookUrlPattern())) {
                    Loger.showLog(tag, ">搜索结果为详情页");
                    SearchBookBean item = getItem(analyzer, baseUrl);
                    if (item != null) {
                        item.setBookInfoHtml(body);
                        books.add(item);
                    }
                } else {
                    initRule();
                    List<Object> collections;
                    boolean reverse = false;
                    boolean allInOne = false;
                    if (ruleList.startsWith("-")) {
                        reverse = true;
                        ruleList = ruleList.substring(1);
                    }
                    // 仅使用java正则表达式提取书籍列表
                    if (ruleList.startsWith(":")) {
                        ruleList = ruleList.substring(1);
                        Loger.showLog(tag, "┌解析搜索列表");
                        getBooksOfRegex(body, ruleList.split("&&"), 0, analyzer, books);
                    } else {
                        if (ruleList.startsWith("+")) {
                            allInOne = true;
                            ruleList = ruleList.substring(1);
                        }
                        //获取列表
                        Loger.showLog(tag, "┌解析搜索列表");
                        collections = analyzer.getElements(ruleList);
                        if (collections.size() == 0 && isEmpty(bookSourceBean.getRuleBookUrlPattern())) {
                            Loger.showLog(tag, "└搜索列表为空,当做详情页处理");
                            SearchBookBean item = getItem(analyzer, baseUrl);
                            if (item != null) {
                                item.setBookInfoHtml(body);
                                books.add(item);
                            }
                        } else {
                            Loger.showLog(tag, "└找到 " + collections.size() + " 个匹配的结果");
                            if (allInOne) {
                                for (int i = 0; i < collections.size(); i++) {
                                    Object object = collections.get(i);
                                    SearchBookBean item = getItemAllInOne(analyzer, object, baseUrl, i == 0);
                                    if (item != null) {
                                        //如果网址相同则缓存
                                        if (baseUrl.equals(item.getNoteUrl())) {
                                            item.setBookInfoHtml(body);
                                        }
                                        books.add(item);
                                    }
                                }
                            } else {
                                for (int i = 0; i < collections.size(); i++) {
                                    Object object = collections.get(i);
                                    analyzer.setContent(object, baseUrl);
                                    SearchBookBean item = getItemInList(analyzer, baseUrl, i == 0);
                                    if (item != null) {
                                        //如果网址相同则缓存
                                        if (baseUrl.equals(item.getNoteUrl())) {
                                            item.setBookInfoHtml(body);
                                        }
                                        books.add(item);
                                    }
                                }
                            }
                        }
                    }
                    if (books.size() > 1 && reverse) {
                        Collections.reverse(books);
                    }
                }
                if (books.isEmpty()) {
                    e.onError(new Throwable(SEARCH_ERR_DATA));
                    return;
                }
                Loger.showLog(tag, "-书籍列表解析结束");
                e.onNext(books);
                e.onComplete();
            }
        });
    }

    private void initRule() {
        if (isFind && !TextUtils.isEmpty(bookSourceBean.getRuleFindList())) {
            ruleList = bookSourceBean.getRuleFindList();
            ruleName = bookSourceBean.getRuleFindName();
            ruleAuthor = bookSourceBean.getRuleFindAuthor();
            ruleKind = bookSourceBean.getRuleFindKind();
            ruleIntroduce = bookSourceBean.getRuleFindIntroduce();
            ruleCoverUrl = bookSourceBean.getRuleFindCoverUrl();
            ruleLastChapter = bookSourceBean.getRuleFindLastChapter();
            ruleNoteUrl = bookSourceBean.getRuleFindNoteUrl();
        } else {
            ruleList = bookSourceBean.getRuleSearchList();
            ruleName = bookSourceBean.getRuleSearchName();
            ruleAuthor = bookSourceBean.getRuleSearchAuthor();
            ruleKind = bookSourceBean.getRuleSearchKind();
            ruleIntroduce = bookSourceBean.getRuleSearchIntroduce();
            ruleCoverUrl = bookSourceBean.getRuleSearchCoverUrl();
            ruleLastChapter = bookSourceBean.getRuleSearchLastChapter();
            ruleNoteUrl = bookSourceBean.getRuleSearchNoteUrl();
        }
    }

    /**
     * 详情页
     */
    private SearchBookBean getItem(AnalyzeRule analyzer, String baseUrl) throws Exception {
        SearchBookBean item = new SearchBookBean();
        analyzer.setBook(item);
        item.setTag(tag);
        item.setOrigin(sourceName);
        item.setNoteUrl(baseUrl);
        // 获取详情页预处理规则
        String ruleInfoInit = bookSourceBean.getRuleBookInfoInit();
        if (!isEmpty(ruleInfoInit)) {
            // 仅使用java正则表达式提取书籍详情
            if (ruleInfoInit.startsWith(":")) {
                ruleInfoInit = ruleInfoInit.substring(1);
                Loger.showLog(tag, "┌详情信息预处理");
                BookShelfBean bookShelfBean = new BookShelfBean();
                bookShelfBean.setTag(tag);
                bookShelfBean.setNoteUrl(baseUrl);
                AnalyzeByRegex.getInfoOfRegex(String.valueOf(analyzer.getContent()), ruleInfoInit.split("&&"), 0, bookShelfBean, analyzer, bookSourceBean, tag);
                if (isEmpty(bookShelfBean.getBookInfoBean().getName())) return null;
                item.setName(bookShelfBean.getBookInfoBean().getName());
                item.setAuthor(bookShelfBean.getBookInfoBean().getAuthor());
                item.setCoverUrl(bookShelfBean.getBookInfoBean().getCoverUrl());
                item.setLastChapter(bookShelfBean.getLastChapterName());
                item.setIntroduce(bookShelfBean.getBookInfoBean().getIntroduce());
                return item;
            } else {
                Object object = analyzer.getElement(ruleInfoInit);
                if (object != null) {
                    analyzer.setContent(object);
                }
            }
        }
        Loger.showLog(tag, ">书籍网址:" + baseUrl);
//        Loger.showLog(tag, "┌获取书名");
        String bookName = StringUtils.formatHtml(analyzer.getString(bookSourceBean.getRuleBookName()));
//        Loger.showLog(tag, "└" + bookName);
        if (!TextUtils.isEmpty(bookName)) {
            item.setName(bookName);
//            Loger.showLog(tag, "┌获取作者");
            item.setAuthor(StringUtils.formatHtml(analyzer.getString(bookSourceBean.getRuleBookAuthor())));
//            Loger.showLog(tag, "└" + item.getAuthor());
//            Loger.showLog(tag, "┌获取分类");
            item.setKind(analyzer.getString(bookSourceBean.getRuleBookKind()));
//            Loger.showLog(tag, "└" + item.getKind());
//            Loger.showLog(tag, "┌获取最新章节");
            item.setLastChapter(analyzer.getString(bookSourceBean.getRuleBookLastChapter()));
//            Loger.showLog(tag, "└" + item.getLastChapter());
//            Loger.showLog(tag, "┌获取简介");
            item.setIntroduce(analyzer.getString(bookSourceBean.getRuleIntroduce()));
//            Loger.showLog(tag, "└" + item.getIntroduce());
//            Loger.showLog(tag, "┌获取封面");
            item.setCoverUrl(analyzer.getString(bookSourceBean.getRuleCoverUrl(), true));
//            Loger.showLog(tag, "└" + item.getCoverUrl());
            return item;
        }
        return null;
    }

    private SearchBookBean getItemAllInOne(AnalyzeRule analyzer, Object object, String baseUrl, boolean printLog) {
        SearchBookBean item = new SearchBookBean();
        analyzer.setBook(item);
        NativeObject nativeObject = (NativeObject) object;
        Loger.showLog(tag, "┌获取书名");
        String bookName = StringUtils.formatHtml(String.valueOf(nativeObject.get(ruleName)));
        Loger.showLog(tag, "└" + bookName);
        if (!isEmpty(bookName)) {
            item.setTag(tag);
            item.setOrigin(sourceName);
            item.setName(bookName);
            Loger.showLog(tag, "┌获取作者");
            item.setAuthor(StringUtils.formatHtml(String.valueOf(nativeObject.get(ruleAuthor))));
            Loger.showLog(tag, "└" + item.getAuthor());
            Loger.showLog(tag, "┌获取分类");
            item.setKind(String.valueOf(nativeObject.get(ruleKind)));
            Loger.showLog(tag, "└" + item.getKind());
            Loger.showLog(tag, "┌获取最新章节");
            item.setLastChapter(String.valueOf(nativeObject.get(ruleLastChapter)));
            Loger.showLog(tag, "└" + item.getLastChapter());
            Loger.showLog(tag, "┌获取简介");
            item.setIntroduce(String.valueOf(nativeObject.get(ruleIntroduce)));
            Loger.showLog(tag, "└" + item.getIntroduce());
            Loger.showLog(tag, "┌获取封面");
            if (!isEmpty(ruleCoverUrl))
                item.setCoverUrl(NetworkUtils.getAbsoluteURL(baseUrl, String.valueOf(nativeObject.get(ruleCoverUrl))));
            Loger.showLog(tag, "└" + item.getCoverUrl());
            Loger.showLog(tag, "┌获取书籍网址");
            String resultUrl = String.valueOf(nativeObject.get(ruleNoteUrl));
            if (isEmpty(resultUrl)) resultUrl = baseUrl;
            item.setNoteUrl(resultUrl);
            Loger.showLog(tag, "└" + item.getNoteUrl());
            return item;
        }
        return null;
    }

    private SearchBookBean getItemInList(AnalyzeRule analyzer, String baseUrl, boolean printLog) throws
            Exception {
        SearchBookBean item = new SearchBookBean();
        analyzer.setBook(item);
        Loger.showLog(tag, "┌获取书名");
        String bookName = StringUtils.formatHtml(analyzer.getString(ruleName));
        Loger.showLog(tag, "└" + bookName);
        if (!TextUtils.isEmpty(bookName)) {
            item.setTag(tag);
            item.setOrigin(sourceName);
            item.setName(bookName);
            Loger.showLog(tag, "┌获取作者");
            item.setAuthor(StringUtils.formatHtml(analyzer.getString(ruleAuthor)));
            Loger.showLog(tag, "└" + item.getAuthor());
            Loger.showLog(tag, "┌获取分类");
            item.setKind(analyzer.getString(ruleKind));
            Loger.showLog(tag, "└" + item.getKind());
            Loger.showLog(tag, "┌获取最新章节");
            item.setLastChapter(analyzer.getString(ruleLastChapter));
            Loger.showLog(tag, "└" + item.getLastChapter());
            Loger.showLog(tag, "┌获取简介");
            item.setIntroduce(analyzer.getString(ruleIntroduce));
            Loger.showLog(tag, "└" + item.getIntroduce());
            Loger.showLog(tag, "┌获取封面");
            item.setCoverUrl(analyzer.getString(ruleCoverUrl));
            Loger.showLog(tag, "└" + item.getCoverUrl());
            Loger.showLog(tag, "┌获取书籍网址");
            String resultUrl = analyzer.getString(ruleNoteUrl, true);
            if (isEmpty(resultUrl)) resultUrl = baseUrl;
            item.setNoteUrl(resultUrl);
            Loger.showLog(tag, "└" + item.getNoteUrl());
            return item;
        }
        return null;
    }

    // 纯java模式正则表达式获取书籍列表
    private void getBooksOfRegex(String res, String[] regs,
                                 int index, AnalyzeRule analyzer, final List<SearchBookBean> books) throws Exception {
        Matcher resM = Pattern.compile(regs[index]).matcher(res);
        String baseUrl = analyzer.getBaseUrl();
        // 判断规则是否有效,当搜索列表规则无效时当作详情页处理
        if (!resM.find()) {
            books.add(getItem(analyzer, baseUrl));
            return;
        }
        // 判断索引的规则是最后一个规则
        if (index + 1 == regs.length) {
            // 获取规则列表
            HashMap<String, String> ruleMap = new HashMap<>();
            ruleMap.put("ruleName", ruleName);
            ruleMap.put("ruleAuthor", ruleAuthor);
            ruleMap.put("ruleKind", ruleKind);
            ruleMap.put("ruleLastChapter", ruleLastChapter);
            ruleMap.put("ruleIntroduce", ruleIntroduce);
            ruleMap.put("ruleCoverUrl", ruleCoverUrl);
            ruleMap.put("ruleNoteUrl", ruleNoteUrl);
            // 分离规则参数
            List<String> ruleName = new ArrayList<>();
            List<List<String>> ruleParams = new ArrayList<>();  // 创建规则参数容器
            List<List<Integer>> ruleTypes = new ArrayList<>();  // 创建规则类型容器
            List<Boolean> hasVarParams = new ArrayList<>();     // 创建put&get标志容器
            for (String key : ruleMap.keySet()) {
                String val = ruleMap.get(key);
                ruleName.add(key);
                hasVarParams.add(!TextUtils.isEmpty(val) && (val.contains("@put") || val.contains("@get")));
                List<String> ruleParam = new ArrayList<>();
                List<Integer> ruleType = new ArrayList<>();
                AnalyzeByRegex.splitRegexRule(val, ruleParam, ruleType);
                ruleParams.add(ruleParam);
                ruleTypes.add(ruleType);
            }
            // 提取书籍列表
            do {
                // 新建书籍容器
                SearchBookBean item = new SearchBookBean(tag, sourceName);
                analyzer.setBook(item);
                // 提取规则内容
                HashMap<String, String> ruleVal = new HashMap<>();
                StringBuilder infoVal = new StringBuilder();
                for (int i = ruleParams.size(); i-- > 0; ) {
                    List<String> ruleParam = ruleParams.get(i);
                    List<Integer> ruleType = ruleTypes.get(i);
                    infoVal.setLength(0);
                    for (int j = ruleParam.size(); j-- > 0; ) {
                        int regType = ruleType.get(j);
                        if (regType > 0) {
                            infoVal.insert(0, resM.group(regType));
                        } else if (regType < 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            infoVal.insert(0, resM.group(ruleParam.get(j)));
                        } else {
                            infoVal.insert(0, ruleParam.get(j));
                        }
                    }
                    ruleVal.put(ruleName.get(i), hasVarParams.get(i) ? AnalyzeByRegex.checkKeys(infoVal.toString(), analyzer) : infoVal.toString());
                }
                // 保存当前节点的书籍信息
                item.setSearchInfo(
                        StringUtils.formatHtml(ruleVal.get("ruleName")),        // 保存书名
                        StringUtils.formatHtml(ruleVal.get("ruleAuthor")),      // 保存作者
                        ruleVal.get("ruleKind"),        // 保存分类
                        ruleVal.get("ruleLastChapter"), // 保存终章
                        ruleVal.get("ruleIntroduce"),   // 保存简介
                        ruleVal.get("ruleCoverUrl"),    // 保存封面
                        NetworkUtils.getAbsoluteURL(baseUrl, ruleVal.get("ruleNoteUrl"))       // 保存详情
                );
                books.add(item);
                // 判断搜索结果是否为详情页
                if (books.size() == 1 && (isEmpty(ruleVal.get("ruleNoteUrl")) || ruleVal.get("ruleNoteUrl").equals(baseUrl))) {
                    books.get(0).setNoteUrl(baseUrl);
                    books.get(0).setBookInfoHtml(res);
                    return;
                }
            } while (resM.find());
            // 输出调试信息
            Loger.showLog(tag, "└找到 " + books.size() + " 个匹配的结果");
            Loger.showLog(tag, "┌获取书名");
            Loger.showLog(tag, "└" + books.get(0).getName());
            Loger.showLog(tag, "┌获取作者");
            Loger.showLog(tag, "└" + books.get(0).getAuthor());
            Loger.showLog(tag, "┌获取分类");
            Loger.showLog(tag, "└" + books.get(0).getKind());
            Loger.showLog(tag, "┌获取最新章节");
            Loger.showLog(tag, "└" + books.get(0).getLastChapter());
            Loger.showLog(tag, "┌获取简介");
            Loger.showLog(tag, "└" + books.get(0).getIntroduce());
            Loger.showLog(tag, "┌获取封面");
            Loger.showLog(tag, "└" + books.get(0).getCoverUrl());
            Loger.showLog(tag, "┌获取书籍");
            Loger.showLog(tag, "└" + books.get(0).getNoteUrl());
        } else {
            StringBuilder result = new StringBuilder();
            do {
                result.append(resM.group());
            } while (resM.find());
            getBooksOfRegex(result.toString(), regs, ++index, analyzer, books);
        }
    }
}