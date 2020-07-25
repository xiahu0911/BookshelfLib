package com.flyersoft.source.conf;

import com.flyersoft.source.BuildConfig;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


/**
 * Created By huzheng
 * Date 2020/5/7
 * Des
 */
public class Consts {

    public static final long TIME_OUT = BuildConfig.DEBUG ? 600 : 180;

    public static Type MAP_STRING = new TypeToken<Map<String, String>>() {
    }.getType();

    public static final Pattern JS_PATTERN = Pattern.compile("(<js>[\\w\\W]*?</js>|@js:[\\w\\W]*$)", Pattern.CASE_INSENSITIVE);
    public static final Pattern EXP_PATTERN = Pattern.compile("\\{\\{([\\w\\W]*?)\\}\\}");

    public static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("rhino");
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36";

    //源测试
    public static final int TEST_STATE_DEFAULT = 0;
    public static final int TEST_STATE_START = 1;
    public static final int TEST_STATE_FAILD = -1;
    public static final int TEST_STATE_SUCCESS = 2;

    //书源的类型
    public static final int YUEDU_2_0 = 0;//阅读2.0
    public static final int YUEDU_3_0 = 1;//阅读3.0
    public static final int HHM_2_0 = 2;//坏坏猫2.0
    public static final int HOUMO_2_0 = 3;//厚墨2.0
}
