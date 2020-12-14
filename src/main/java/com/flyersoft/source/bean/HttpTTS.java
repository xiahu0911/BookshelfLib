package com.flyersoft.source.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created By huzheng
 * Date 2020/12/10
 * Des tts
 */
@Entity
public class HttpTTS {
    @Id
    private Long id;
    private String name;
    private String url;
    @Generated(hash = 1534037303)
    public HttpTTS(Long id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }
    @Generated(hash = 168365370)
    public HttpTTS() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
}
