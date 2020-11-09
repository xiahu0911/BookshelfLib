package com.flyersoft.source.bean;

import com.flyersoft.source.utils.JsonUtils;
import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static com.flyersoft.source.conf.Consts.MAP_STRING;

@Entity
public class SearchBookBean implements BaseBookBean {
    @Id
    private String noteUrl;
    private String coverUrl;//封面URL
    private String name;
    private String author;
    private String tag;
    private String kind;//分类
    private String origin;//来源
    private String lastChapter;
    private String introduce; //简介
    private String chapterUrl;//目录URL
    public String wordCount;
    private Long addTime = 0L;

    @Transient
    private Boolean isCurrentSource = false;
    @Transient
    private int originNum = 1;
    @Transient
    private int lastChapterNum = -2;
    @Transient
    private int searchTime = Integer.MAX_VALUE;
    @Transient
    private LinkedHashSet<String> originUrls;
    private Long upTime = 0L;
    @Transient
    private String variable = "";
    @Transient
    private Map<String, String> variableMap;
    @Transient
    private String bookInfoHtml;

    public SearchBookBean() {

    }

    public SearchBookBean(String tag, String origin) {
        this.tag = tag;
        this.origin = origin;
    }

    @Generated(hash = 119917703)
    public SearchBookBean(String noteUrl, String coverUrl, String name, String author, String tag, String kind, String origin,
            String lastChapter, String introduce, String chapterUrl, String wordCount, Long addTime, Long upTime) {
        this.noteUrl = noteUrl;
        this.coverUrl = coverUrl;
        this.name = name;
        this.author = author;
        this.tag = tag;
        this.kind = kind;
        this.origin = origin;
        this.lastChapter = lastChapter;
        this.introduce = introduce;
        this.chapterUrl = chapterUrl;
        this.wordCount = wordCount;
        this.addTime = addTime;
        this.upTime = upTime;
    }

    @Override
    public String getVariable() {
        return this.variable;
    }

    @Override
    public void setVariable(String variable) {
        this.variable = variable;
    }

    @Override
    public void putVariable(String key, String value) {
        if (variableMap == null) {
            variableMap = JsonUtils.gsonToMaps(variable);
        }
        if (variableMap == null) {
            variableMap = new HashMap<>();
        }
        variableMap.put(key, value);
        variable = JsonUtils.objectToJson(variableMap);
    }

    @Override
    public Map<String, String> getVariableMap() {
        if (variableMap == null) {
            return new Gson().fromJson(variable, MAP_STRING);
        }
        return variableMap;
    }

    @Override
    public String getNoteUrl() {
        return noteUrl;
    }

    @Override
    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) {
            name = name.trim().replaceAll("　", "");
            if (name.startsWith("《"))
                name = name.replace("《", "").replace("》", "");
        }
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        if (author != null) {
            if (author.startsWith("作者:") || author.startsWith("作者："))
                author = author.substring(3).trim();
        }
        this.author = author;
    }

    public String getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(String lastChapter) {
        if (lastChapter != null) {
            lastChapter = lastChapter.equals("null") ? "" : lastChapter.replace("\n", " ").trim();
        }
        this.lastChapter = lastChapter;

    }

    public int getLastChapterNum() {
//        if (lastChapterNum == -2)
//            this.lastChapterNum = BookshelfHelp.guessChapterNum(lastChapter);
        return lastChapterNum;
//        return 0;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        if (kind != null) {
            kind = kind.replace("\n", " ").replace(",", " ").trim();
        }
        this.kind = kind;
    }

    @Override
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Boolean getIsCurrentSource() {
        return this.isCurrentSource;
    }

    public void setIsCurrentSource(Boolean isCurrentSource) {
        this.isCurrentSource = isCurrentSource;
        if (isCurrentSource)
            this.addTime = System.currentTimeMillis();
    }

    public int getOriginNum() {
        return originNum;
    }

    public void addOriginUrl(String origin) {
        if (this.originUrls == null) {
            this.originUrls = new LinkedHashSet<>();
        }
        this.originUrls.add(origin);
        originNum = this.originUrls.size();
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getChapterUrl() {
        return this.chapterUrl;
    }

    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
    }

    public long getAddTime() {
        return this.addTime;
    }

    public void setAddTime(Long addTime) {
        this.addTime = addTime;
    }

    public int getWeight() {
//        BookSourceBean source = BookSourceManager.getBookSourceByUrl(this.tag);
//        if (source != null)
//            return source.getWeight();
//        else
        return 0;
    }

    public int getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(int searchTime) {
        this.searchTime = searchTime;
    }

    public Long getUpTime() {
        return this.upTime;
    }

    public void setUpTime(Long upTime) {
        this.upTime = upTime;
    }

    public String getBookInfoHtml() {
        return bookInfoHtml;
    }

    public void setBookInfoHtml(String bookInfoHtml) {
        this.bookInfoHtml = bookInfoHtml;
    }

    // 一次性存入搜索书籍节点信息
    public void setSearchInfo(String name, String author, String kind, String lastChapter,
                              String introduce, String coverUrl, String noteUrl) {
        this.name = name;
        this.author = author;
        this.kind = kind;
        this.lastChapter = lastChapter;
        this.introduce = introduce;
        this.coverUrl = coverUrl;
        this.noteUrl = noteUrl;
    }

    public String getWordCount() {
        return this.wordCount;
    }

    public void setWordCount(String wordCount) {
        this.wordCount = wordCount;
    }
}
