package com.flyersoft.source.bean.source3;

import android.text.TextUtils;

import com.flyersoft.source.bean.source3.BookInfoRule;
import com.flyersoft.source.bean.source3.ContentRule;
import com.flyersoft.source.bean.source3.ExploreRule;
import com.flyersoft.source.bean.source3.SearchRule;
import com.flyersoft.source.bean.source3.TocRule;

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
 * Des 3.0书源实体
 */
public class BookSource3 {

    private String bookSourceName;                // 名称
    private  String bookSourceGroup;            // 分组
    private  String bookSourceUrl;                 // 地址，包括 http/https
    private  String bookSourceType = "0";     // 类型，0 文本，1 音频
    private  String bookUrlPattern;             // 详情页url正则
    private  String customOrder;                       // 手动排序编号
    private  boolean enabled;                    // 是否启用
    private  boolean enabledExplore;             // 启用发现
    private  String header;                     // 请求头
    private  String loginUrl;                   // 登录地址
    private  String lastUpdateTime;                   // 最后更新时间，用于排序
    private  String weight;                            // 智能排序的权重
    private  String exploreUrl;                 // 发现url
    private  Object ruleExplore;           // 发现规则
    private  String searchUrl;                  // 搜索url
    private  Object ruleSearch;             // 搜索规则
    private  Object ruleBookInfo;         // 书籍信息页规则
    private  Object ruleToc;                   // 目录页规则
    private  Object ruleContent;            // 正文页规则

    public String getBookSourceName() {
        return bookSourceName;
    }

    public void setBookSourceName(String bookSourceName) {
        this.bookSourceName = bookSourceName;
    }

    public String getBookSourceGroup() {
        return bookSourceGroup;
    }

    public void setBookSourceGroup(String bookSourceGroup) {
        this.bookSourceGroup = bookSourceGroup;
    }

    public String getBookSourceUrl() {
        return bookSourceUrl;
    }

    public void setBookSourceUrl(String bookSourceUrl) {
        this.bookSourceUrl = bookSourceUrl;
    }

    public String getBookSourceType() {
        return bookSourceType;
    }

    public void setBookSourceType(String bookSourceType) {
        this.bookSourceType = bookSourceType;
    }

    public String getBookUrlPattern() {
        return bookUrlPattern;
    }

    public void setBookUrlPattern(String bookUrlPattern) {
        this.bookUrlPattern = bookUrlPattern;
    }

    public String getCustomOrder() {
        return customOrder;
    }

    public void setCustomOrder(String customOrder) {
        this.customOrder = customOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabledExplore() {
        return enabledExplore;
    }

    public void setEnabledExplore(boolean enabledExplore) {
        this.enabledExplore = enabledExplore;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getExploreUrl() {
        return exploreUrl;
    }

    public void setExploreUrl(String exploreUrl) {
        this.exploreUrl = exploreUrl;
    }

    public Object getRuleExplore() {
        return ruleExplore;
    }

    public void setRuleExplore(Object ruleExplore) {
        this.ruleExplore = ruleExplore;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }

    public Object getRuleSearch() {
        return ruleSearch;
    }

    public void setRuleSearch(Object ruleSearch) {
        this.ruleSearch = ruleSearch;
    }

    public Object getRuleBookInfo() {
        return ruleBookInfo;
    }

    public void setRuleBookInfo(Object ruleBookInfo) {
        this.ruleBookInfo = ruleBookInfo;
    }

    public Object getRuleToc() {
        return ruleToc;
    }

    public void setRuleToc(Object ruleToc) {
        this.ruleToc = ruleToc;
    }

    public Object getRuleContent() {
        return ruleContent;
    }

    public void setRuleContent(Object ruleContent) {
        this.ruleContent = ruleContent;
    }
}