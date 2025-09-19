package com.dspread.pos.utils;

import com.google.gson.Gson;

public class JsonUtil {
    private static final Gson gson = new Gson();

    /**
     * 将任意对象（包括泛型T）序列化为JSON字符串
     * @param object 要转换的对象（如response.getResult()返回的T）
     * @return JSON字符串
     */
    public static String toJsonString(Object object) {
        if (object == null) {
            return "[]"; // 空对象返回空数组，避免解析报错
        }
        return gson.toJson(object);
    }
}
