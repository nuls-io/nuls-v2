package io.nuls.test.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.core.core.config.ConfigSetting;
import io.nuls.core.parse.JSONUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-19 21:20
 * @Description: 功能描述
 */
public class Utils {


    static Map<Class, Function<String, Object>> transfer = new HashMap<>();

    static {
        transfer.put(Integer.class, Integer::parseInt);
        transfer.put(int.class, Integer::parseInt);
        transfer.put(Long.class, Long::parseLong);
        transfer.put(long.class, Long::parseLong);
        transfer.put(Float.class, Float::parseFloat);
        transfer.put(float.class, Float::parseFloat);
        transfer.put(Double.class, Double::parseDouble);
        transfer.put(double.class, Double::parseDouble);
        transfer.put(Character.class, str -> str.charAt(0));
        transfer.put(char.class, str -> str.charAt(0));
        transfer.put(Short.class, Short::parseShort);
        transfer.put(short.class, Short::parseShort);
        transfer.put(Boolean.class, Boolean::parseBoolean);
        transfer.put(boolean.class, Boolean::parseBoolean);
        transfer.put(Byte.class, Byte::parseByte);
        transfer.put(byte.class, Byte::parseByte);
        transfer.put(String.class, str -> str);

    }


    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void success(String msg){
        System.out.println(ANSI_GREEN + msg +  ANSI_RESET);
    }

    public static void fail(String msg){
        System.out.println(ANSI_RED + msg +  ANSI_RESET);
    }

    public static void msg(String msg){
        System.out.println(ANSI_BLUE + msg + ANSI_RESET);
    }

    public static void failDoubleLine(String msg){
        fail("=".repeat(msg.length()*2));
        fail(msg);
        fail("=".repeat(msg.length()*2));
    }

    public static void failLine(String msg){
        fail("-".repeat(msg.length()*2));
        fail(msg);
        fail("-".repeat(msg.length()*2));
    }

    public static void successDoubleLine(String msg){
        success("=".repeat(msg.length()*2));
        success(msg);
        success("=".repeat(msg.length()*2));
    }

    public static void successLine(String msg){
        success("-".repeat(msg.length()*2));
        success(msg);
        success("-".repeat(msg.length()*2));
    }

    public static String toJson(Object obj){
        Class<?> cls = obj.getClass();
        if(ConfigSetting.isPrimitive(cls)){
            return String.valueOf(obj);
        }else{
            try {
                return JSONUtils.obj2json(obj);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Object jsonToObj(String str,Class<?>  cls ){
        if(ConfigSetting.isPrimitive(cls)){
            return transfer.get(cls).apply(str);
        }else{
            try {
                return JSONUtils.json2pojo(str,cls);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
