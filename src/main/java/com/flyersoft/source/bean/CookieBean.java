package com.flyersoft.source.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class CookieBean {

    @Id
    private String url;
    private String cookie;
    @Generated(hash = 517179762)
    public CookieBean(String url, String cookie) {
        this.url = url;
        this.cookie = cookie;
    }
    @Generated(hash = 769081142)
    public CookieBean() {
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getCookie() {
        return this.cookie;
    }
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
