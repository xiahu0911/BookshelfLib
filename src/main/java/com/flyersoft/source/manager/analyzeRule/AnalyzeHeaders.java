package com.flyersoft.source.manager.analyzeRule;

import com.flyersoft.source.bean.BookSource;
import com.flyersoft.source.bean.CookieBean;
import com.flyersoft.source.conf.Consts;
import com.flyersoft.source.conf.Keys;
import com.flyersoft.source.dao.CookieController;
import com.flyersoft.source.utils.JsonUtils;
import com.flyersoft.source.utils.SPUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;
import javax.script.SimpleBindings;

import static android.text.TextUtils.isEmpty;
import static com.flyersoft.source.conf.Consts.SCRIPT_ENGINE;

/**
 * 解析Headers
 */
public class AnalyzeHeaders {

    public static Map<String, String> getMap(BookSource bookSourceBean) {
        Map<String, String> headerMap = new HashMap<>();
        try {
            //2.0
            if (bookSourceBean.getFrom() == Consts.YUEDU_2_0) {
                if (bookSourceBean != null && !isEmpty(bookSourceBean.getHttpUserAgent())) {
                    headerMap.put("User-Agent", bookSourceBean.getHttpUserAgent());
                } else {
                    headerMap.put("User-Agent", getDefaultUserAgent());
                }
                if (bookSourceBean != null) {
                    CookieBean cookie = CookieController.getInstance().getCookie(bookSourceBean.getBookSourceUrl());
                    if (cookie != null) {
                        headerMap.put("Cookie", cookie.getCookie());
                    }
                }
            }
            if (bookSourceBean.getFrom() == Consts.YUEDU_3_0) {
                headerMap.put("User-Agent", getDefaultUserAgent());
                if (bookSourceBean != null && !isEmpty(bookSourceBean.getHttpUserAgent())) {
                    String ua = bookSourceBean.getHttpUserAgent();
                    if (StringUtils.startsWithIgnoreCase(ua, "<js>")) {
                        ua = ua.substring(4, ua.lastIndexOf("<"));
                        ua = evalJS(ua);
                    } else if (StringUtils.startsWithIgnoreCase(bookSourceBean.getHttpUserAgent(), "@js:")) {
                        ua = ua.substring(4);
                        ua = evalJS(ua);
                    }
                    Map<String, String> stringObjectMap = JsonUtils.gsonToMaps(ua);
                    if (stringObjectMap != null) {
                        headerMap.putAll(stringObjectMap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return headerMap;
        }
        return headerMap;
    }

    private static String getDefaultUserAgent() {
        return SPUtils.getInformain(Keys.SP_KEY_USER_AGENT, Consts.DEFAULT_USER_AGENT);
    }

    /**
     * 执行JS
     */
    private static String evalJS(String jsStr) {
        try {
            return SCRIPT_ENGINE.eval(jsStr).toString();
        } catch (ScriptException e) {
            e.printStackTrace();
            return "";
        }
    }
}
