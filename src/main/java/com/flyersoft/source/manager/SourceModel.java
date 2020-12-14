package com.flyersoft.source.manager;

import com.flyersoft.source.base.IHttpGetApi;
import com.flyersoft.source.bean.BookSource;
import com.flyersoft.source.bean.source3.BookInfoRule;
import com.flyersoft.source.bean.source3.BookSource3;
import com.flyersoft.source.bean.source3.ContentRule;
import com.flyersoft.source.bean.source3.ExploreRule;
import com.flyersoft.source.bean.source3.SearchRule;
import com.flyersoft.source.bean.source3.TocRule;
import com.flyersoft.source.conf.Consts;
import com.flyersoft.source.manager.analyzeRule.AnalyzeHeaders;
import com.flyersoft.source.utils.JsonUtils;
import com.flyersoft.source.utils.Loger;
import com.flyersoft.source.utils.NetworkUtils;
import com.flyersoft.source.utils.RxUtils;
import com.flyersoft.source.utils.StringUtils;
import com.google.gson.Gson;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import retrofit2.Response;

import static com.flyersoft.source.utils.NetworkUtils.pagePattern;

/**
 * Created By huzheng
 * Date 2020/5/7
 * Des 书籍爬虫
 */
public class SourceModel extends BaseModel {

    public static Observable<List<BookSource>> importSource(String string) {
        if (StringUtils.isTrimEmpty(string)) return null;
        string = string.trim();
        if (NetworkUtils.isIPv4Address(string)) {
            string = String.format("http://%s:65501", string);
        }
        if (StringUtils.isJsonType(string)) {
            return importBookSourceFromJson(string.trim())
                    .compose(new ObservableTransformer<List<BookSource>, List<BookSource>>() {
                        @Override
                        public ObservableSource<List<BookSource>> apply(Observable<List<BookSource>> upstream) {
                            return RxUtils.toSimpleSingle(upstream);
                        }
                    });
        }
        if (NetworkUtils.isUrl(string)) {
            return BaseModel.getInstance().getRetrofitString(StringUtils.getBaseUrl(string), "utf-8")
                    .create(IHttpGetApi.class)
                    .get(string, AnalyzeHeaders.getMap(null))
                    .flatMap(new Function<Response<String>, ObservableSource<List<BookSource>>>() {
                        @Override
                        public ObservableSource<List<BookSource>> apply(Response<String> stringResponse) throws Exception {
                            return importBookSourceFromJson(stringResponse.body());
                        }
                    })
                    .map(new Function<List<BookSource>, List<BookSource>>() {
                        @Override
                        public List<BookSource> apply(List<BookSource> bookSources) throws Exception {
                            for (BookSource bookSource : bookSources) {
                                bookSource.setBookSourceName(keepOnlyFilenameChars(bookSource.getBookSourceName()));
                            }
                            return bookSources;
                        }
                    })
                    .compose(new ObservableTransformer<List<BookSource>, List<BookSource>>() {
                        @Override
                        public ObservableSource<List<BookSource>> apply(Observable<List<BookSource>> upstream) {
                            return RxUtils.toSimpleSingle(upstream);
                        }
                    });
        }
        return Observable.error(new Throwable("不是Json或Url格式"));
    }


    private static Observable<List<BookSource>> importBookSourceFromJson(final String json) {
        return Observable.create(new ObservableOnSubscribe<List<BookSource>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BookSource>> e) throws Exception {

                List<BookSource> bookSourceBeans = new ArrayList<>();
                List<BookSource3> bookSourceBeans3 = new ArrayList<>();
                String text = json.trim();
                //json对象
                if (StringUtils.isJsonObject(text)) {
                    DocumentContext parse = JsonPath.parse(text);
                    List<String> urls = parse.read("$.sourceUrls");
                    if (urls == null || urls.size() < 1) {
                        BookSource bookSource = convertSource(text);
                        if (bookSource != null) {
                            bookSourceBeans.add(bookSource);
                            e.onNext(bookSourceBeans);
                            e.onComplete();
                            return;
                        }
                    } else {
                        //批量网络导入
//                        for (String url : urls) {
//                            importSourceUrls(url);
//                        }
                        e.onNext(bookSourceBeans);
                        e.onComplete();
                    }
                } else if (StringUtils.isJsonArray(json)) {
                    //json数组
                    List<Map<String, Object>> expensiveBooks = JsonPath
                            .using(Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build())
                            .parse(json)
                            .read("$", List.class);
                    for (Map<String, Object> map : expensiveBooks) {
                        DocumentContext parse = JsonPath.parse(map);
                        BookSource bookSource = convertSource(parse.jsonString());
                        if (bookSource != null)
                            bookSourceBeans.add(bookSource);
                    }
                    e.onNext(bookSourceBeans);
                    e.onComplete();
                    return;
                }
                e.onError(new Throwable("格式不对"));
            }
        });
    }

    //将3.0源转换成格式
    private static BookSource convertSource(String jsonItem) {
        if (jsonItem.isEmpty()) {
            return null;
        }
        try {
            BookSource3 bookSource3 = JsonUtils.gsonToObject(jsonItem, BookSource3.class);
            if ((bookSource3 == null || bookSource3.getRuleToc() == null) && !bookSource3.getBookSourceType().equals("100")) {
                //2.0直接导入
                BookSource bookSource = JsonUtils.gsonToObject(jsonItem, BookSource.class);
                //如果不是大师导出的源，需要设置一下来源
                if (!bookSource.getFlyersoft()) {
                    bookSource.setFrom(Consts.YUEDU_2_0);
                }
                return bookSource;
            } else {
                //3.0进行转换
                BookSource bookSource = new BookSource();
                bookSource.setFrom(Consts.YUEDU_3_0);
                bookSource.setBookSourceUrl(bookSource3.getBookSourceUrl());
                bookSource.setBookSourceName(bookSource3.getBookSourceName());
                bookSource.setBookSourceGroup(bookSource3.getBookSourceGroup());
                bookSource.setLoginUrl(bookSource3.getLoginUrl());
                bookSource.setRuleBookUrlPattern(bookSource3.getBookUrlPattern());
                bookSource.setSerialNumber(Integer.parseInt(bookSource3.getCustomOrder() == null ? "0" : bookSource3.getCustomOrder()));
                bookSource.setHttpUserAgent(bookSource3.getHeader());
                bookSource.setRuleSearchUrl(bookSource3.getSearchUrl());
                bookSource.setRuleFindUrl(bookSource3.getExploreUrl());
                bookSource.setBookSourceType(bookSource3.getBookSourceType() + "");
                bookSource.setEnable(bookSource3.isEnabled());
                //搜索
                String s = "";
                if (bookSource3.getRuleSearch() instanceof String) {
                    s = JsonPath.parse(bookSource3.getRuleSearch().toString()).jsonString();
                } else {
                    s = JsonPath.parse(JsonUtils.objectToJson(bookSource3.getRuleSearch())).jsonString();
                }
                SearchRule ruleSearch = JsonUtils.gsonToObject(s, SearchRule.class);
                bookSource.setRuleSearchList(ruleSearch.getBookList());
                bookSource.setRuleSearchName(ruleSearch.getName());
                bookSource.setRuleSearchAuthor(ruleSearch.getAuthor());
                bookSource.setRuleSearchIntroduce(ruleSearch.getIntro());
                bookSource.setRuleSearchKind(ruleSearch.getKind());
                bookSource.setRuleSearchNoteUrl(ruleSearch.getBookUrl());
                bookSource.setRuleSearchCoverUrl(ruleSearch.getCoverUrl());
                bookSource.setRuleSearchLastChapter(ruleSearch.getLastChapter());
                bookSource.setRuleSearchUpdateTime(ruleSearch.getUpdateTime());
                bookSource.setRuleSearchWordCount(ruleSearch.getWordCount());
                //发现(如果发现规则不存在，则与搜索规则保持一致)
                if (bookSource3.getRuleExplore() != null) {
                    if (bookSource3.getRuleExplore() instanceof String) {
                        s = JsonPath.parse(bookSource3.getRuleExplore().toString()).jsonString();
                    } else {
                        s = JsonPath.parse(JsonUtils.objectToJson(bookSource3.getRuleExplore())).jsonString();
                    }
                    ExploreRule exploreRule = JsonUtils.gsonToObject(s, ExploreRule.class);
                    bookSource.setRuleFindList(exploreRule.getBookList());
                    bookSource.setRuleFindName(exploreRule.getName());
                    bookSource.setRuleFindAuthor(exploreRule.getAuthor());
                    bookSource.setRuleFindIntroduce(exploreRule.getIntro());
                    bookSource.setRuleFindKind(exploreRule.getKind());
                    bookSource.setRuleFindNoteUrl(exploreRule.getBookUrl());
                    bookSource.setRuleFindCoverUrl(exploreRule.getCoverUrl());
                    bookSource.setRuleFindLastChapter(exploreRule.getLastChapter());
                }
                //详情
                if (bookSource3.getRuleBookInfo() != null) {
                    if (bookSource3.getRuleBookInfo() instanceof String) {
                        s = JsonPath.parse(bookSource3.getRuleBookInfo().toString()).jsonString();
                    } else {
                        s = JsonPath.parse(JsonUtils.objectToJson(bookSource3.getRuleBookInfo())).jsonString();
                    }
                    BookInfoRule bookInfoRule = JsonUtils.gsonToObject(s, BookInfoRule.class);
                    bookSource.setRuleBookInfoInit(bookInfoRule.getInit());
                    bookSource.setRuleBookName(bookInfoRule.getName());
                    bookSource.setRuleBookAuthor(bookInfoRule.getAuthor());
                    bookSource.setRuleIntroduce(bookInfoRule.getIntro());
                    bookSource.setRuleBookKind(bookInfoRule.getKind());
                    bookSource.setRuleCoverUrl(bookInfoRule.getCoverUrl());
                    bookSource.setRuleBookLastChapter(bookInfoRule.getLastChapter());
                    bookSource.setRuleChapterUrl(bookInfoRule.getTocUrl());
                    bookSource.setRuleBookUpdateTime(bookInfoRule.getUpdateTime());
                    bookSource.setRuleBookWordCount(bookInfoRule.getWordCount());
                }

                //章节
                if (bookSource3.getRuleToc() != null) {
                    if (bookSource3.getRuleToc() instanceof String) {
                        s = JsonPath.parse(bookSource3.getRuleToc().toString()).jsonString();
                    } else {
                        s = JsonPath.parse(JsonUtils.objectToJson(bookSource3.getRuleToc())).jsonString();
                    }
                    TocRule tocRule = JsonUtils.gsonToObject(s, TocRule.class);
                    bookSource.setRuleChapterList(tocRule.getChapterList());
                    bookSource.setRuleChapterName(tocRule.getChapterName());
                    bookSource.setRuleContentUrl(tocRule.getChapterUrl());
                    bookSource.setRuleChapterUrlNext(tocRule.getNextTocUrl());
                    bookSource.setRuleChapterUpdateTime(tocRule.getUpdateTime());
                    bookSource.setIsVip(tocRule.getIsVip());
                }

                //内容
                if (bookSource3.getRuleContent() != null) {
                    if (bookSource3.getRuleContent() instanceof String) {
                        s = JsonPath.parse(bookSource3.getRuleContent().toString()).jsonString();
                    } else {
                        s = JsonPath.parse(JsonUtils.objectToJson(bookSource3.getRuleContent())).jsonString();
                    }
                    ContentRule contentRule = JsonUtils.gsonToObject(s, ContentRule.class);
                    bookSource.setRuleBookContent(contentRule.getContent());
                    bookSource.setRuleContentUrlNext(contentRule.getNextContentUrl());
                    bookSource.setRuleBookContentSourceRegex(contentRule.getSourceRegex());
                    bookSource.setRuleBookContentWebJs(contentRule.getWebJs());
                    bookSource.setRuleBookContentReplaceRegex(contentRule.getReplaceRegex());
                }

                return bookSource;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //批量网络导入
    private static BookSource importSourceUrls(String url) {
        return null;
    }

    // default规则适配
    // #正则#替换内容 替换成 ##正则##替换内容
    // | 替换成 ||
    // & 替换成 &&
    private static String toNewRule(String newRule) {
        if (newRule == null) return "";
        String oldRule = newRule;
        boolean reverse = false;
        boolean allinone = false;
        boolean web = false;
        if (newRule.startsWith("-")) {
            reverse = true;
            oldRule = newRule.substring(1);
        }
        if (oldRule.startsWith("+")) {
            allinone = true;
            oldRule = oldRule.substring(1);
        }
        if (!StringUtils.startWithIgnoreCase(oldRule, "@CSS:") &&
                !StringUtils.startWithIgnoreCase(oldRule, "@XPath:") &&
                !oldRule.startsWith("//") &&
                !oldRule.startsWith("##") &&
                !oldRule.startsWith(":") &&
                !StringUtils.containWithIgnoreCase(oldRule, "@js:") &&
                !StringUtils.containWithIgnoreCase(oldRule, "<js>")
        ) {
            if (oldRule.contains("##")) {
                oldRule = newRule.replace("##", "#");
            }
            if (oldRule.contains("||")) {
                if (oldRule.contains("#")) {
                    String[] list = oldRule.split("#");
                    if (list[0].contains("||")) {
                        oldRule = list[0].replace("||", "|");
                        for (int i = 1; i < list.length; i++) {
                            oldRule += "#" + list[i];
                        }
                    }
                } else {
                    oldRule = oldRule.replace("||", "|");
                }
            }
            if (oldRule.contains("&&")) {
                oldRule = oldRule.replace("&&", "&");
            }
        }
        if (allinone) {
            oldRule = "+" + oldRule;
        }
        if (reverse) {
            oldRule = "-" + oldRule;
        }
        return oldRule;
    }


    private static String toNewUrls(String oldUrls) {
        if (oldUrls == null) return "";
//        if (oldUrls.contains("\n") || oldUrls.contains("&&"))
//            return toNewUrl(oldUrls);

        String[] urls = oldUrls.split("(&&|\r?\n)+");
//        return urls
        StringBuilder stringBuilder = new StringBuilder();
        for (String url : urls) {
            String newUrl = toNewUrl(url);
            if (StringUtils.isNotEmpty(newUrl)) {
                stringBuilder.append(newUrl).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    //处理3.0Url
    public static String toNewUrl(String oldUrl) {
        if (oldUrl == null) return "";
        String url = oldUrl;
        if (StringUtils.startWithIgnoreCase(oldUrl, "<js>")) {
            url = url.replace("={{key}}", "=searchKey")
                    .replace("={{page}}", "=searchPage");
            return url;
        }

        //处理页码
        String page = "";
        Matcher matcher = pagePattern.matcher(url);
        while (matcher.find()) {
            page = matcher.group();
            if (StringUtils.isNotEmpty(page))
                url = url.replace(page, "");
        }


        String[] urlList = url.split(",[^\\{]*", 2);
        url = urlList[0];
        UrlOption urlOption = null;
        if (urlList.length > 1) {
            if (StringUtils.isNotEmpty(urlList[1])) {
                urlOption = JsonUtils.gsonToObject(urlList[1], UrlOption.class);
            }
        }
        //处理url
        url = url.replace("{{key}}", "searchKey")
                .replace("{{page}}", "searchPage")
                .replace("{{page+1}}", "searchPage+1")
                .replace("{{page-1}}", "searchPage-1");
        if (urlOption != null) {
            if (urlOption.getBody() != null && !urlOption.getBody().isEmpty()) {
                url = url + "@" + urlOption.getBody().replace("{{key}}", "searchKey")
                        .replace("{{page}}", "searchPage")
                        .replace("{{page+1}}", "searchPage+1")
                        .replace("{{page-1}}", "searchPage-1");
                ;
            }
            if (urlOption.getHeaders() != null && !urlOption.getHeaders().isEmpty()) {
                String headers = urlOption.getHeaders();
                url = "@Header:" + (headers.startsWith("{") ? "" : "{") + headers + (headers.endsWith("}") ? "" : "}") + url;
            }
            if (urlOption.getCharset() != null && !urlOption.getCharset().isEmpty()) {
                url = url + "|" + "char=" + urlOption.getCharset();
            }
        }
        return url;
    }


    private static String uaToHeader(String ua) {
        if (ua == null || ua.isEmpty()) return "";
//        Map<String, String> stringObjectMap = JsonUtils.gsonToMaps(ua);
//        return stringObjectMap.get("User-Agent");
        return ua;
    }


    /**
     * 规范源名称
     * 方便本地存储
     *
     * @param str
     * @return
     */
    private static String keepOnlyFilenameChars(String str) {
        try {
            Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
            Matcher m = pattern.matcher(str);
            return m.replaceAll("").trim().replaceAll("\n", "").replaceAll("#", "");
        } catch (Exception e) {
            return str;
        }
    }

    //3.0Url配置
    class UrlOption {
        String method;
        String charset;
        String webView;
        String headers;
        String body;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }

        public String getWebView() {
            return webView;
        }

        public void setWebView(String webView) {
            this.webView = webView;
        }

        public String getHeaders() {
            return headers;
        }

        public void setHeaders(String headers) {
            this.headers = headers;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }

}
