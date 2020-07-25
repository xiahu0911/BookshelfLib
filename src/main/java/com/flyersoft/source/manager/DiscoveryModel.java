package com.flyersoft.source.manager;

import android.text.TextUtils;

import com.flyersoft.source.bean.BookSource;
import com.flyersoft.source.bean.DiscoveryBean;
import com.flyersoft.source.bean.DiscoveryKindBean;
import com.flyersoft.source.bean.DiscoveryKindGroupBean;
import com.flyersoft.source.dao.SourceController;
import com.flyersoft.source.manager.analyzeRule.AnalyzeRule;
import com.flyersoft.source.utils.RxUtils;

import java.util.ArrayList;
import java.util.List;

import javax.script.SimpleBindings;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.disposables.Disposable;

import static com.flyersoft.source.conf.Consts.SCRIPT_ENGINE;

/**
 * Created By huzheng
 * Date 2020/5/7
 * Des 发现
 */
public class DiscoveryModel extends BaseModel {

    private Disposable disposable;
    private AnalyzeRule analyzeRule;
    private String findError = "发现规则语法错误";

    @SuppressWarnings("unchecked")
    public void getData(final DiscoveryListener discoveryListener, final boolean bySelect) {
        if (disposable != null) return;
//        ACache aCache = ACache.get(mView.getContext(), "findCache");
        Single.create(new SingleOnSubscribe<List<DiscoveryBean>>() {
            @Override
            public void subscribe(SingleEmitter<List<DiscoveryBean>> e) throws Exception {
                List<DiscoveryBean> group = new ArrayList<>();
//            boolean showAllFind = MApplication.getConfigPreferences().getBoolean("showAllFind", true);
                List<BookSource> all = new ArrayList<>();
                if (!bySelect) {
                    all.addAll(SourceController.getInstance().getAll());
                } else {
                    all.addAll(SourceController.getInstance().getAllSelect());
                }
                List<BookSource> sourceBeans = new ArrayList<>(all);
                for (BookSource sourceBean : sourceBeans) {
                    DiscoveryBean discoveryBean = convertDeiscovery(sourceBean);
                    if (discoveryBean != null) {
                        group.add(discoveryBean);
                    }
                }
                e.onSuccess(group);
            }
        })
                .compose(new SingleTransformer<List<DiscoveryBean>, List<DiscoveryBean>>() {
                    @Override
                    public SingleSource<List<DiscoveryBean>> apply(Single<List<DiscoveryBean>> upstream) {
                        return RxUtils.toSimpleSingle(upstream);
                    }
                })
                .subscribe(new SingleObserver<List<DiscoveryBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onSuccess(List<DiscoveryBean> recyclerViewData) {
                        discoveryListener.onSuccess(recyclerViewData);
                        disposable.dispose();
                        disposable = null;
                    }

                    @Override
                    public void onError(Throwable e) {
                        discoveryListener.onError(e.getMessage());
                        disposable.dispose();
                        disposable = null;
                    }
                });
    }

    public DiscoveryBean convertDeiscovery(BookSource sourceBean) {
        DiscoveryBean discoveryBean = null;
        try {
            String[] kindA;
            String findRule = "";
            if (!TextUtils.isEmpty(sourceBean.getRuleFindUrl())) {
                boolean isJsAndCache = sourceBean.getRuleFindUrl().startsWith("<js>");
                if (isJsAndCache) {
                    if (TextUtils.isEmpty(findRule)) {
                        String jsStr = sourceBean.getRuleFindUrl().substring(4, sourceBean.getRuleFindUrl().lastIndexOf("<"));
                        findRule = evalJS(jsStr, sourceBean.getBookSourceUrl()).toString();
                    }
                } else {
                    findRule = sourceBean.getRuleFindUrl();
                }
                kindA = findRule.split("(&&|\n)+");
                List<DiscoveryKindBean> children = new ArrayList<>();
                for (String kindB : kindA) {
                    if (kindB.trim().isEmpty()||kindB.trim().equals("null")) continue;
                    String[] kind = kindB.split("::");
                    DiscoveryKindBean discoveryKindBean = new DiscoveryKindBean();
                    discoveryKindBean.setGroup(sourceBean.getBookSourceName());
                    discoveryKindBean.setTag(sourceBean.getBookSourceUrl());
                    discoveryKindBean.setKindName(kind[0]);
                    if (kind.length > 1) {
                        discoveryKindBean.setKindUrl(kind[1]);
                    } else {
                        discoveryKindBean.setKindUrl(sourceBean.getBookSourceUrl());
                    }
                    children.add(discoveryKindBean);
                }
                DiscoveryKindGroupBean groupBean = new DiscoveryKindGroupBean();
                groupBean.setSourceName(sourceBean.getBookSourceName());
                groupBean.setGroupTag(sourceBean.getBookSourceUrl());
                discoveryBean = new DiscoveryBean(groupBean, children);
            }
        } catch (Exception exception) {
            sourceBean.addGroup(findError);
        }
        return discoveryBean;
    }

    /**
     * 执行JS
     */
    private Object evalJS(String jsStr, String baseUrl) throws Exception {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("java", getAnalyzeRule());
        bindings.put("baseUrl", baseUrl);
        return SCRIPT_ENGINE.eval(jsStr, bindings);
    }

    private AnalyzeRule getAnalyzeRule() {
        if (analyzeRule == null) {
            analyzeRule = new AnalyzeRule(null);
        }
        return analyzeRule;
    }

    public interface DiscoveryListener {
        void onSuccess(List<DiscoveryBean> data);

        void onError(String e);
    }

}
