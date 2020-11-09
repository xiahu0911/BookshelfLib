//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.flyersoft.source.bean;

import com.flyersoft.source.utils.JsonUtils;
import com.google.gson.Gson;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 章节列表
 */
@Entity
public class BookChapterBean implements Cloneable {
    private String tag;
    private String noteUrl; //对应BookInfoBean noteUrl;

    private int index;  //当前章节数
    @Id
    private String url;  //当前章节对应的文章地址
    private String title;  //当前章节名称

    //章节内容在文章中的起始位置(本地)
    private Long start;
    //章节内容在文章中的终止位置(本地)
    private Long end;
    @Transient
    private String variable;
    @Transient
    private HashMap<String, String> variableMap = new HashMap<>(2);

    public BookChapterBean() {
    }

    public BookChapterBean(String tag, String durChapterName, String durChapterUrl) {
        this.tag = tag;
        this.title = durChapterName;
        this.url = durChapterUrl;
    }

    @Generated(hash = 1729848268)
    public BookChapterBean(String tag, String noteUrl, int index, String url, String title,
            Long start, Long end) {
        this.tag = tag;
        this.noteUrl = noteUrl;
        this.index = index;
        this.url = url;
        this.title = title;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Object clone() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(this);
            return gson.fromJson(json, BookChapterBean.class);
        } catch (Exception ignored) {
        }
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BookChapterBean) {
            BookChapterBean bookChapterBean = (BookChapterBean) obj;
            return Objects.equals(bookChapterBean.url, url);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (url == null) {
            return 0;
        }
        return url.hashCode();
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getNoteUrl() {
        return this.noteUrl;
    }

    public void setNoteUrl(String noteUrl) {
        this.noteUrl = noteUrl;
    }

    public Long getStart() {
        return this.start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return this.end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public void putVariable(String key, String value) {
        variableMap.put(key, value);
        variable = JsonUtils.objectToJson(variableMap);
    }

    public Map<String, String> getVariableMap() {
        if (variableMap == null) {
            Map<String, String> stringObjectMap = JsonUtils.gsonToMaps(variable);
            if (stringObjectMap == null) {
                return new HashMap<String, String>();
            }
            return stringObjectMap;
        }
        return variableMap;
    }
}
