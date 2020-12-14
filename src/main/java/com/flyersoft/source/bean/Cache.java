package com.flyersoft.source.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class Cache {

    @Id
    private String key;
    private String value;
    private Long deadline = 0L;
    @Generated(hash = 224937513)
    public Cache(String key, String value, Long deadline) {
        this.key = key;
        this.value = value;
        this.deadline = deadline;
    }
    @Generated(hash = 1305017356)
    public Cache() {
    }
    public String getKey() {
        return this.key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getValue() {
        return this.value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public Long getDeadline() {
        return this.deadline;
    }
    public void setDeadline(Long deadline) {
        this.deadline = deadline;
    }
}
