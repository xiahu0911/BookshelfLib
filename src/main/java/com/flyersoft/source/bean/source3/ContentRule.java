package com.flyersoft.source.bean.source3;

/**
 * Created By huzheng
 * Date 2020/7/8
 * Des
 */
public class ContentRule {

    private String content;
    private String nextContentUrl;
    private String webJs;
    private String sourceRegex;
    private String replaceRegex;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNextContentUrl() {
        return nextContentUrl;
    }

    public void setNextContentUrl(String nextContentUrl) {
        this.nextContentUrl = nextContentUrl;
    }

    public String getWebJs() {
        return webJs;
    }

    public void setWebJs(String webJs) {
        this.webJs = webJs;
    }

    public String getSourceRegex() {
        return sourceRegex;
    }

    public void setSourceRegex(String sourceRegex) {
        this.sourceRegex = sourceRegex;
    }

    public String getReplaceRegex() {
        return replaceRegex;
    }

    public void setReplaceRegex(String replaceRegex) {
        this.replaceRegex = replaceRegex;
    }
}
