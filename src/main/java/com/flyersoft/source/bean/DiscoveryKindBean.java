package com.flyersoft.source.bean;

public class DiscoveryKindBean {
    private String group;
    private String tag;
    private String kindName;
    private String kindUrl;

    public DiscoveryKindBean() {

    }

    public String getKindName() {
//        if (kindName != null)
//            return kindName.replace(" ", "").trim();
        return kindName == null? "" : kindName;
    }

    public void setKindName(String kindName) {
        this.kindName = kindName;
    }

    public String getKindUrl() {
        return kindUrl;
    }

    public void setKindUrl(String kindUrl) {
        this.kindUrl = kindUrl;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

}
