package com.flyersoft.source.bean;

import android.text.TextUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.Transient;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

/**
 * Created By huzheng
 * Date 2020/4/30
 * Des 书源实体
 */
@Entity
public class BookSource {

    @Id
    private String bookSourceUrl;
    private String bookSourceName;
    private String bookSourceGroup;
    private String bookSourceType;
    private String loginUrl;
    private Long lastUpdateTime;
    private int from;//用来标记书源的来源（0：默认阅读2.0; 1:阅读3.0）
    @OrderBy
    private int serialNumber;
    @OrderBy
    @NotNull
    private int weight = 0;
    private boolean enable;
    //发现规则
    private String ruleFindUrl;
    private String ruleFindList;
    private String ruleFindName;
    private String ruleFindAuthor;
    private String ruleFindKind;
    private String ruleFindIntroduce;
    private String ruleFindLastChapter;
    private String ruleFindCoverUrl;
    private String ruleFindNoteUrl;
    //搜索规则
    private String ruleSearchUrl;
    private String ruleSearchList;
    private String ruleSearchName;
    private String ruleSearchAuthor;
    private String ruleSearchKind;
    private String ruleSearchIntroduce;
    private String ruleSearchLastChapter;
    private String ruleSearchCoverUrl;
    private String ruleSearchNoteUrl;
    //详情页规则
    private String ruleBookUrlPattern;
    private String ruleBookInfoInit;
    private String ruleBookName;
    private String ruleBookAuthor;
    private String ruleCoverUrl;
    private String ruleIntroduce;
    private String ruleBookKind;
    private String ruleBookLastChapter;
    private String ruleChapterUrl;
    //目录页规则
    private String ruleChapterUrlNext;
    private String ruleChapterList;
    private String ruleChapterName;
    private String ruleContentUrl;
    //正文页规则
    private String ruleContentUrlNext;
    private String ruleBookContent;
    private String httpUserAgent;

    @Transient
    private transient ArrayList<String> groupList;
    @Transient
    private int testState;//测试状态(0:未开始，1：开始测试，2：成功，-1：失败)
    @Transient
    private String testMsg;//测试结果
    @Transient
    private long testTime;//测试结果
    @Transient
    public Disposable d;//测试程序

    @Generated(hash = 754688599)
    public BookSource(String bookSourceUrl, String bookSourceName,
            String bookSourceGroup, String bookSourceType, String loginUrl,
            Long lastUpdateTime, int from, int serialNumber, int weight,
            boolean enable, String ruleFindUrl, String ruleFindList,
            String ruleFindName, String ruleFindAuthor, String ruleFindKind,
            String ruleFindIntroduce, String ruleFindLastChapter,
            String ruleFindCoverUrl, String ruleFindNoteUrl, String ruleSearchUrl,
            String ruleSearchList, String ruleSearchName, String ruleSearchAuthor,
            String ruleSearchKind, String ruleSearchIntroduce,
            String ruleSearchLastChapter, String ruleSearchCoverUrl,
            String ruleSearchNoteUrl, String ruleBookUrlPattern,
            String ruleBookInfoInit, String ruleBookName, String ruleBookAuthor,
            String ruleCoverUrl, String ruleIntroduce, String ruleBookKind,
            String ruleBookLastChapter, String ruleChapterUrl,
            String ruleChapterUrlNext, String ruleChapterList,
            String ruleChapterName, String ruleContentUrl,
            String ruleContentUrlNext, String ruleBookContent,
            String httpUserAgent) {
        this.bookSourceUrl = bookSourceUrl;
        this.bookSourceName = bookSourceName;
        this.bookSourceGroup = bookSourceGroup;
        this.bookSourceType = bookSourceType;
        this.loginUrl = loginUrl;
        this.lastUpdateTime = lastUpdateTime;
        this.from = from;
        this.serialNumber = serialNumber;
        this.weight = weight;
        this.enable = enable;
        this.ruleFindUrl = ruleFindUrl;
        this.ruleFindList = ruleFindList;
        this.ruleFindName = ruleFindName;
        this.ruleFindAuthor = ruleFindAuthor;
        this.ruleFindKind = ruleFindKind;
        this.ruleFindIntroduce = ruleFindIntroduce;
        this.ruleFindLastChapter = ruleFindLastChapter;
        this.ruleFindCoverUrl = ruleFindCoverUrl;
        this.ruleFindNoteUrl = ruleFindNoteUrl;
        this.ruleSearchUrl = ruleSearchUrl;
        this.ruleSearchList = ruleSearchList;
        this.ruleSearchName = ruleSearchName;
        this.ruleSearchAuthor = ruleSearchAuthor;
        this.ruleSearchKind = ruleSearchKind;
        this.ruleSearchIntroduce = ruleSearchIntroduce;
        this.ruleSearchLastChapter = ruleSearchLastChapter;
        this.ruleSearchCoverUrl = ruleSearchCoverUrl;
        this.ruleSearchNoteUrl = ruleSearchNoteUrl;
        this.ruleBookUrlPattern = ruleBookUrlPattern;
        this.ruleBookInfoInit = ruleBookInfoInit;
        this.ruleBookName = ruleBookName;
        this.ruleBookAuthor = ruleBookAuthor;
        this.ruleCoverUrl = ruleCoverUrl;
        this.ruleIntroduce = ruleIntroduce;
        this.ruleBookKind = ruleBookKind;
        this.ruleBookLastChapter = ruleBookLastChapter;
        this.ruleChapterUrl = ruleChapterUrl;
        this.ruleChapterUrlNext = ruleChapterUrlNext;
        this.ruleChapterList = ruleChapterList;
        this.ruleChapterName = ruleChapterName;
        this.ruleContentUrl = ruleContentUrl;
        this.ruleContentUrlNext = ruleContentUrlNext;
        this.ruleBookContent = ruleBookContent;
        this.httpUserAgent = httpUserAgent;
    }
    @Generated(hash = 2045691642)
    public BookSource() {
    }
    public String getBookSourceUrl() {
        return this.bookSourceUrl;
    }
    public void setBookSourceUrl(String bookSourceUrl) {
        this.bookSourceUrl = bookSourceUrl;
    }
    public String getBookSourceName() {
        return this.bookSourceName;
    }
    public void setBookSourceName(String bookSourceName) {
        this.bookSourceName = bookSourceName;
    }
    public String getBookSourceGroup() {
        return this.bookSourceGroup;
    }
    public void setBookSourceGroup(String bookSourceGroup) {
        this.bookSourceGroup = bookSourceGroup;
    }
    public String getBookSourceType() {
        return this.bookSourceType;
    }
    public void setBookSourceType(String bookSourceType) {
        this.bookSourceType = bookSourceType;
    }
    public String getLoginUrl() {
        return this.loginUrl;
    }
    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }
    public Long getLastUpdateTime() {
        return this.lastUpdateTime;
    }
    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
    public int getSerialNumber() {
        return this.serialNumber;
    }
    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }
    public int getWeight() {
        return this.weight;
    }
    public void setWeight(int weight) {
        this.weight = weight;
    }
    public boolean getEnable() {
        return this.enable;
    }
    public void setEnable(boolean enable) {
        this.enable = enable;
    }
    public String getRuleFindUrl() {
        return this.ruleFindUrl;
    }
    public void setRuleFindUrl(String ruleFindUrl) {
        this.ruleFindUrl = ruleFindUrl;
    }
    public String getRuleFindList() {
        return this.ruleFindList;
    }
    public void setRuleFindList(String ruleFindList) {
        this.ruleFindList = ruleFindList;
    }
    public String getRuleFindName() {
        return this.ruleFindName;
    }
    public void setRuleFindName(String ruleFindName) {
        this.ruleFindName = ruleFindName;
    }
    public String getRuleFindAuthor() {
        return this.ruleFindAuthor;
    }
    public void setRuleFindAuthor(String ruleFindAuthor) {
        this.ruleFindAuthor = ruleFindAuthor;
    }
    public String getRuleFindKind() {
        return this.ruleFindKind;
    }
    public void setRuleFindKind(String ruleFindKind) {
        this.ruleFindKind = ruleFindKind;
    }
    public String getRuleFindIntroduce() {
        return this.ruleFindIntroduce;
    }
    public void setRuleFindIntroduce(String ruleFindIntroduce) {
        this.ruleFindIntroduce = ruleFindIntroduce;
    }
    public String getRuleFindLastChapter() {
        return this.ruleFindLastChapter;
    }
    public void setRuleFindLastChapter(String ruleFindLastChapter) {
        this.ruleFindLastChapter = ruleFindLastChapter;
    }
    public String getRuleFindCoverUrl() {
        return this.ruleFindCoverUrl;
    }
    public void setRuleFindCoverUrl(String ruleFindCoverUrl) {
        this.ruleFindCoverUrl = ruleFindCoverUrl;
    }
    public String getRuleFindNoteUrl() {
        return this.ruleFindNoteUrl;
    }
    public void setRuleFindNoteUrl(String ruleFindNoteUrl) {
        this.ruleFindNoteUrl = ruleFindNoteUrl;
    }
    public String getRuleSearchUrl() {
        return this.ruleSearchUrl;
    }
    public void setRuleSearchUrl(String ruleSearchUrl) {
        this.ruleSearchUrl = ruleSearchUrl;
    }
    public String getRuleSearchList() {
        return this.ruleSearchList;
    }
    public void setRuleSearchList(String ruleSearchList) {
        this.ruleSearchList = ruleSearchList;
    }
    public String getRuleSearchName() {
        return this.ruleSearchName;
    }
    public void setRuleSearchName(String ruleSearchName) {
        this.ruleSearchName = ruleSearchName;
    }
    public String getRuleSearchAuthor() {
        return this.ruleSearchAuthor;
    }
    public void setRuleSearchAuthor(String ruleSearchAuthor) {
        this.ruleSearchAuthor = ruleSearchAuthor;
    }
    public String getRuleSearchKind() {
        return this.ruleSearchKind;
    }
    public void setRuleSearchKind(String ruleSearchKind) {
        this.ruleSearchKind = ruleSearchKind;
    }
    public String getRuleSearchIntroduce() {
        return this.ruleSearchIntroduce;
    }
    public void setRuleSearchIntroduce(String ruleSearchIntroduce) {
        this.ruleSearchIntroduce = ruleSearchIntroduce;
    }
    public String getRuleSearchLastChapter() {
        return this.ruleSearchLastChapter;
    }
    public void setRuleSearchLastChapter(String ruleSearchLastChapter) {
        this.ruleSearchLastChapter = ruleSearchLastChapter;
    }
    public String getRuleSearchCoverUrl() {
        return this.ruleSearchCoverUrl;
    }
    public void setRuleSearchCoverUrl(String ruleSearchCoverUrl) {
        this.ruleSearchCoverUrl = ruleSearchCoverUrl;
    }
    public String getRuleSearchNoteUrl() {
        return this.ruleSearchNoteUrl;
    }
    public void setRuleSearchNoteUrl(String ruleSearchNoteUrl) {
        this.ruleSearchNoteUrl = ruleSearchNoteUrl;
    }
    public String getRuleBookUrlPattern() {
        return this.ruleBookUrlPattern;
    }
    public void setRuleBookUrlPattern(String ruleBookUrlPattern) {
        this.ruleBookUrlPattern = ruleBookUrlPattern;
    }
    public String getRuleBookInfoInit() {
        return this.ruleBookInfoInit;
    }
    public void setRuleBookInfoInit(String ruleBookInfoInit) {
        this.ruleBookInfoInit = ruleBookInfoInit;
    }
    public String getRuleBookName() {
        return this.ruleBookName;
    }
    public void setRuleBookName(String ruleBookName) {
        this.ruleBookName = ruleBookName;
    }
    public String getRuleBookAuthor() {
        return this.ruleBookAuthor;
    }
    public void setRuleBookAuthor(String ruleBookAuthor) {
        this.ruleBookAuthor = ruleBookAuthor;
    }
    public String getRuleCoverUrl() {
        return this.ruleCoverUrl;
    }
    public void setRuleCoverUrl(String ruleCoverUrl) {
        this.ruleCoverUrl = ruleCoverUrl;
    }
    public String getRuleIntroduce() {
        return this.ruleIntroduce;
    }
    public void setRuleIntroduce(String ruleIntroduce) {
        this.ruleIntroduce = ruleIntroduce;
    }
    public String getRuleBookKind() {
        return this.ruleBookKind;
    }
    public void setRuleBookKind(String ruleBookKind) {
        this.ruleBookKind = ruleBookKind;
    }
    public String getRuleBookLastChapter() {
        return this.ruleBookLastChapter;
    }
    public void setRuleBookLastChapter(String ruleBookLastChapter) {
        this.ruleBookLastChapter = ruleBookLastChapter;
    }
    public String getRuleChapterUrl() {
        return this.ruleChapterUrl;
    }
    public void setRuleChapterUrl(String ruleChapterUrl) {
        this.ruleChapterUrl = ruleChapterUrl;
    }
    public String getRuleChapterUrlNext() {
        return this.ruleChapterUrlNext;
    }
    public void setRuleChapterUrlNext(String ruleChapterUrlNext) {
        this.ruleChapterUrlNext = ruleChapterUrlNext;
    }
    public String getRuleChapterList() {
        return this.ruleChapterList;
    }
    public void setRuleChapterList(String ruleChapterList) {
        this.ruleChapterList = ruleChapterList;
    }
    public String getRuleChapterName() {
        return this.ruleChapterName;
    }
    public void setRuleChapterName(String ruleChapterName) {
        this.ruleChapterName = ruleChapterName;
    }
    public String getRuleContentUrl() {
        return this.ruleContentUrl;
    }
    public void setRuleContentUrl(String ruleContentUrl) {
        this.ruleContentUrl = ruleContentUrl;
    }
    public String getRuleContentUrlNext() {
        return this.ruleContentUrlNext;
    }
    public void setRuleContentUrlNext(String ruleContentUrlNext) {
        this.ruleContentUrlNext = ruleContentUrlNext;
    }
    public String getRuleBookContent() {
        return this.ruleBookContent;
    }
    public void setRuleBookContent(String ruleBookContent) {
        this.ruleBookContent = ruleBookContent;
    }
    public String getHttpUserAgent() {
        return this.httpUserAgent;
    }
    public void setHttpUserAgent(String httpUserAgent) {
        this.httpUserAgent = httpUserAgent;
    }

    public int getTestState() {
        return testState;
    }

    public void setTestState(int testState) {
        this.testState = testState;
    }

    public String getTestMsg() {
        return testMsg;
    }

    public void setTestMsg(String testMsg) {
        this.testMsg = testMsg;
    }

    public long getTestTime() {
        return testTime;
    }

    public void setTestTime(long testTime) {
        this.testTime = testTime;
    }

    public boolean containsGroup(String group) {
        if (groupList == null) {
            upGroupList();
        }
        return groupList.contains(group);
    }

    private void upGroupList() {
        if (groupList == null)
            groupList = new ArrayList<>();
        else
            groupList.clear();
        if (!TextUtils.isEmpty(bookSourceGroup)) {
            for (String group : bookSourceGroup.split("\\s*[,;，；]\\s*")) {
                group = group.trim();
                if (TextUtils.isEmpty(group) || groupList.contains(group)) continue;
                groupList.add(group);
            }
        }
    }

    public void addGroup(String group) {
        if (groupList == null)
            upGroupList();
        if (!groupList.contains(group)) {
            groupList.add(group);
//            updateModTime();
            bookSourceGroup = TextUtils.join("; ", groupList);
        }
    }
    public int getFrom() {
        return this.from;
    }
    public void setFrom(int from) {
        this.from = from;
    }
}
