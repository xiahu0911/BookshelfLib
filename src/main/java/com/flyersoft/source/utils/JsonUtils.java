package com.flyersoft.source.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created By huzheng
 * Date 2020-02-29
 * Des 基于Gson封装的jsonUtil
 * 实际开发中我们会遇到比较复杂的JSON，这个时候就需要我们自定义序列化或者反序列化的方法了
 */
public class JsonUtils {

    private static Gson gson = null;

    static {
        if (gson == null) {
            gson = new Gson();
        }
    }

    private JsonUtils() {
    }

    /**
     * 对象转成json
     *
     * @param object
     * @return json
     */
    public static String objectToJson(Object object) {
        String json = null;
        if (gson != null) {
            json = gson.toJson(object);
        }
        return json;
    }

    /**
     * Json转成对象
     *
     * @param json
     * @param cls
     * @return 对象
     */
    public static <T> T gsonToObject(String json, Class<T> cls) {
        T t = null;
        if (gson != null) {
            t = gson.fromJson(json, cls);
        }
        return t;
    }


    /**
     * 将Json数组解析成相应的映射对象List
     */
    public static <T> List<T> gsonToList(String jsonData, Class<T> type) {
        List<T> result = null;
        if (!TextUtils.isEmpty(jsonData)) {
            Gson gson = new GsonBuilder().create();
            try {
                JsonParser parser = new JsonParser();
                JsonArray JArray = parser.parse(jsonData).getAsJsonArray();
                if (JArray != null) {
                    result = new ArrayList<>();
                    for (JsonElement obj : JArray) {
                        try {
                            T cse = gson.fromJson(obj, type);
                            result.add(cse);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    /**
     * 转成list
     * 解决泛型问题
     * @param json
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> List<T> jsonToList(String json, Class<T> cls) {
        Gson gson = new Gson();
        List<T> list = new ArrayList<T>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for(final JsonElement elem : array){
            list.add(gson.fromJson(elem, cls));
        }
        return list;
    }


    /**
     * json转成list中有map的
     *
     * @param json
     * @return List<Map < String, T>>
     */
    public static <T> List<Map<String, T>> gsonToListMaps(String json) {
        List<Map<String, T>> list = null;
        if (gson != null) {
            list = gson.fromJson(json, new TypeToken<List<Map<String, T>>>() {
            }.getType());
        }
        return list;
    }

    /**
     * json转成map的
     *
     * @param json
     * @return Map<String, T>
     */
    public static <T> Map<String, T> gsonToMaps(String json) {
        Map<String, T> map = null;
        if (gson != null) {
            map = gson.fromJson(json, new TypeToken<Map<String, T>>() {
            }.getType());
        }
        return map;
    }

}
