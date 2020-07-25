package com.flyersoft.source.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * sp存储
 */
public class SPUtils {

    private static String SP_KEY = "jdtx";

    private static SharedPreferences settings;

    public static void init(Context context, String key) {
        SP_KEY = key;
        settings = context.getSharedPreferences(
                SP_KEY, Context.MODE_PRIVATE);
    }

    /**
     * 设置信息long
     */
    public static void setInformain(String Key, long Value) {
        Editor editor = settings.edit();
        editor.putLong(Key, Value);
        editor.commit();
    }

    /**
     * 取设置信息String
     */
    public static long getLongInformain(String Key, long defValue) {
        try {
            return settings.getLong(Key, defValue);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return defValue;
    }

    /**
     * 设置信息String
     */
    public static void setInformain(String Key, String Value) {
        Editor editor = settings.edit();
        editor.putString(Key, Value);
        editor.commit();
    }

    /**
     * 取设置信息String
     */
    public static String getInformain(String Key, String defValue) {
        try {
            return settings.getString(Key, defValue);
        } catch (ClassCastException e) {
            // TODO: handle exception
        }
        return defValue;

    }

    /**
     * 取设置信息
     */
    public static int getInformain(String key, int defValue) {
        try {
            return settings.getInt(key, defValue);
        } catch (ClassCastException e) {
            // TODO: handle exception
        }
        return defValue;

    }

    /**
     * 设置信息
     */
    public static void setInformain(String key, int value) {
        Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * 设置信息
     */
    public static void setInformain(String key, boolean value) {
        Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * 取设置信息
     */
    public static boolean getInformain(String key, boolean defaultValue) {
        return settings.getBoolean(key, defaultValue);
    }

    /**
     * 取设置信息
     */
    public static int getInformain(String name, String key, int defaultValue) {
        try {
            return settings.getInt(key, defaultValue);
        } catch (ClassCastException e) {
            // TODO: handle exception
        }
        return defaultValue;

    }

    /**
     * 设置信息
     */
    public static void setInformain(String name, String key, String value) {
        Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 取设置信息
     */
    public static String getInformain(String name, String key, String defValue) {

        try {
            return settings.getString(key, defValue);
        } catch (ClassCastException e) {
            // TODO: handle exception
        }
        return defValue;

    }

    /**
     * 设置信息
     */
    public static void setInformain(String name, String key, int value,
                                    Context context) {

        if (context == null) {
            return;
        }

        SharedPreferences settings = context.getSharedPreferences(name,
                Context.MODE_PRIVATE);
        Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * 清除settings.xml某个键值对
     *
     * @author fei.zhang
     * @date 2Context.MODE_WORLD_READABLE+Context.MODE_WORLD_WRITEABLE+Context.
     * MODE_MULTI_PROCESS12-11-16
     */
    public static void remove(String key) {
        Editor editor = settings.edit().remove(key);
        editor.commit();
    }

}
