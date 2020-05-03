package com.xfhl.common.api.utils;

import java.util.HashMap;
import java.util.Map;

public class ResponseUtil {

    private static Map<String, Object> map = new HashMap<>();// APP要求返回一个空的JSON

    public static Object ok() {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", 10000);
        obj.put("msg", "成功");
        obj.put("data", map);
        return obj;
    }

    public static Object ok(Object data) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", 10000);
        obj.put("msg", "成功");
        obj.put("data", data);
        return obj;
    }
    

    public static Object ok(String msg, Object data) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", 10000);
        obj.put("msg", msg);
        obj.put("data", data);
        return obj;
    }

    public static Object fail() {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", 10502);
        obj.put("msg", "服务器走神了");
        obj.put("data", map);
        return obj;
    }


    public static Object fail(int code, String msg) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", code);
        obj.put("msg", msg);
        obj.put("data", map);
        return obj;
    }

    public static Object fail(String code, String msg) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", code);
        obj.put("msg", msg);
        obj.put("data", map);
        return obj;
    }

    public static Object fail(int errno, String msg, Object data) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", errno);
        obj.put("msg", msg);
        obj.put("data", data);
        return obj;
    }

    public static Object unlogin(){
        //请登录
        return result(10501);
    }

    public static Object result(int code) {
        Map<String, Object> obj = new HashMap<>();
        obj.put("code", code);
        obj.put("msg", ApplicationYmlUtil.get(code));
        obj.put("data",map);
        return obj;
    }

}

