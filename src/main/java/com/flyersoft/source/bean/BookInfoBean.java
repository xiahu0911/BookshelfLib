//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.flyersoft.source.bean;

import android.os.AsyncTask;
import android.text.Html;
import android.text.TextUtils;

import com.flyersoft.source.utils.JsonUtils;
import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 书本信息
 */
//@Entity
public class BookInfoBean implements Cloneable, BaseBookBean {

    private String name; //小说名
    private String tag;
    @Id
    private String noteUrl;  //如果是来源网站,则小说根地址,如果是本地则是小说本地MD5
    private String chapterUrl;  //章节目录地址,本地目录正则
    private long finalRefreshData;  //章节最后更新时间
    private String coverUrl; //小说封面
    private String author;//作者
    private String introduce; //简介
    private String origin; //来源
    private String charset;//编码
    private String bookSourceType;
    private String kind;
    private String wordCount;
    private String latestChapterTitle;
    private int totalChapterNum;//章节数
    @Transient
    private String bookInfoHtml;
    @Transient
    private String chapterListHtml;
    @Transient
    private String variable = "";
    @Transient
    private Map<String, String> variableMap = new HashMap<>();

    public BookInfoBean() {
    }

    @Generated(hash = 1274114508)
    public BookInfoBean(String name, String tag, String noteUrl, String chapterUrl, long finalRefreshData, String coverUrl, String author, String introduce, String origin,
            String charset, String bookSourceType, String kind, String wordCount, String latestChapterTitle) {
        this.name = name;
        this.tag = tag;
        this.noteUrl = noteUrl;
        this.chapterUrl = chapterUrl;
        this.finalRefreshData = finalRefreshData;
        this.coverUrl = coverUrl;
        this.author = author;
        this.introduce = introduce;
        this.origin = origin;
        this.charset = charset;
        this.bookSourceType = bookSourceType;
        this.kind = kind;
        this.wordCount = wordCount;
        this.latestChapterTitle = latestChapterTitle;
    }

    @Override
    protected Object clone() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(this);
            return gson.fromJson(json, BookInfoBean.class);
        } catch (Exception ignored) {
        }
        return this;
    }

    public String getName() {
        return clean(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getNoteUrl() {
        return noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    @Override
    public String getVariable() {
        return variable;
    }

    @Override
    public void setVariable(String variable) {
        this.variable = variable;
        variableMap = JsonUtils.gsonToMaps(variable);
    }

    public void setVariableMap(Map<String, String> variableMap) {
        this.variableMap = variableMap;
        variable = JsonUtils.objectToJson(variableMap);
    }

    @Override
    public void putVariable(String key, String value) {
        variableMap.put(key, value);
        variable = JsonUtils.objectToJson(variableMap);
    }

    @Override
    public Map<String, String> getVariableMap() {
        return variableMap;
    }

    public String getChapterUrl() {
        return chapterUrl;
    }

    public void setChapterUrl(String chapterUrl) {
        this.chapterUrl = chapterUrl;
    }

    public long getFinalRefreshData() {
        return finalRefreshData;
    }

    public void setFinalRefreshData(long finalRefreshData) {
        this.finalRefreshData = finalRefreshData;
    }

    public String getCoverUrl() {
        if (isEpub() && (TextUtils.isEmpty(coverUrl) || !(new File(coverUrl)).exists())) {
            extractEpubCoverImage();
            return "";
        }
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getAuthor() {
        return clean(author);
    }

    private String clean(String s) {
        if (s != null)
            return s.replace("." ,"")
                    .replace("*" ,"")
                    .replace("[" ,"")
                    .replace("]" ,"")
                    .replace("\\" ,"")
                    .replace("/" ,"")
                    .replace("|" ,"");
        return "";
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIntroduce() {
        if (introduce != null && introduce.contains("<"))
            return Html.fromHtml(introduce).toString();
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getCharset() {
        return this.charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    private void extractEpubCoverImage() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
//                try {
//                    FileHelp.createFolderIfNotExists(coverUrl);
//                    Bitmap cover = BitmapFactory.decodeStream(Objects.requireNonNull(PageLoaderEpub.readBook(new File(noteUrl))).getCoverImage().getInputStream());
//                    String md5Path = FileHelp.getCachePath() + File.separator + "cover" + File.separator + MD5Utils.strToMd5By16(noteUrl) + ".jpg";
//                    FileOutputStream out = new FileOutputStream(new File(md5Path));
//                    cover.compress(Bitmap.CompressFormat.JPEG, 90, out);
//                    out.flush();
//                    out.close();
//                    setCoverUrl(md5Path);
//                    DbHelper.getDaoSession().getBookInfoBeanDao().insertOrReplace(BookInfoBean.this);
//                } catch (Exception ignored) {
//                }
            }
        });
    }

    private boolean isEpub() {
        return Objects.equals(tag, BookShelfBean.LOCAL_TAG) && noteUrl.toLowerCase().matches(".*\\.epub$");
    }

    public String getBookSourceType() {
        return this.bookSourceType;
    }

    public void setBookSourceType(String bookSourceType) {
        this.bookSourceType = bookSourceType;
    }

    public boolean isAudio() {
        return false;
    }

    public String getBookInfoHtml() {
        return bookInfoHtml;
    }

    public void setBookInfoHtml(String bookInfoHtml) {
        this.bookInfoHtml = bookInfoHtml;
    }

    public String getChapterListHtml() {
        return chapterListHtml;
    }

    public void setChapterListHtml(String chapterListHtml) {
        this.chapterListHtml = chapterListHtml;
    }

    public String getKind() {
        return this.kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getWordCount() {
        return this.wordCount;
    }

    public void setWordCount(String wordCount) {
        this.wordCount = wordCount;
    }

    public String getLatestChapterTitle() {
        return this.latestChapterTitle;
    }

    public int getTotalChapterNum() {
        return totalChapterNum;
    }

    public void setTotalChapterNum(int totalChapterNum) {
        this.totalChapterNum = totalChapterNum;
    }

    public void setLatestChapterTitle(String latestChapterTitle) {
        this.latestChapterTitle = latestChapterTitle;
    }
}