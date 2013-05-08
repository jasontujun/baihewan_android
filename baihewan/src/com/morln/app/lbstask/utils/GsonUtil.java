package com.morln.app.lbstask.utils;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by 赵之韵.
 * Date: 12-2-29
 * Time: 下午6:55
 */
public class GsonUtil {
    private static Gson gson = new Gson();

    /**
     * 将对象转化为JSON字符串
     */
    public static String toString(Object object) {
        return gson.toJson(object);
    }

    /**
     * 将JSON转化为对象
     *
     * @param content 待转化的JSON字符串
     * @param cls     目标对象的类型
     */
    public static Object toObject(String content, Class cls) {
        return gson.fromJson(content, cls);
    }

    /**
     * 将JSON字符串转化为列表对象
     */
    public static Object toObjectArray(String content, Type type) {
        return gson.fromJson(content, type);
    }

    /**
     * 将列表对象转化为JSON字符串
     */
    public static String toString(Object object, Type type) {

        return gson.toJson(object, type);
    }
}
