package com.flyersoft.source.bean;

import java.util.List;

/**
 * Created By huzheng
 * Date 2020/5/27
 * Des 发现实体
 */
public class DiscoveryBean {

    private DiscoveryKindGroupBean discoveryKindGroupBean;
    private List<DiscoveryKindBean> children;

    public DiscoveryBean(DiscoveryKindGroupBean groupBean, List<DiscoveryKindBean> children) {
        this.discoveryKindGroupBean = groupBean;
        this.children = children;
    }

    public DiscoveryKindGroupBean getDiscoveryKindGroupBean() {
        return discoveryKindGroupBean;
    }

    public String getName(){
        return discoveryKindGroupBean.getSourceName();
    }

    public void setDiscoveryKindGroupBean(DiscoveryKindGroupBean discoveryKindGroupBean) {
        this.discoveryKindGroupBean = discoveryKindGroupBean;
    }

    public List<DiscoveryKindBean> getChildren() {
        return children;
    }

    public void setChildren(List<DiscoveryKindBean> children) {
        this.children = children;
    }
}
