package io.nuls.core.log;

import io.nuls.core.log.logback.LoggerBuilder;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.parse.JSONUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 公共日志类
 * Public Log Class
 *
 * @author tag
 * 2018/12/18
 * */
public class Log {

    public static final String BASIC_NAME = "common";

    public static NulsLogger BASIC_LOGGER = LoggerBuilder.getLogger(BASIC_NAME);

    /**
     * 提供debug级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void debug(String msg) {
        BASIC_LOGGER.debug(msg);
    }

    public static void debug(String msg, Object... objs) {
        try {
            List<String> objStrs = new ArrayList<>();
            for (Object obj: objs) {
                if(obj instanceof String){
                    objStrs.add((String)obj);
                }else{
                    objStrs.add(JSONUtils.obj2json(obj));
                }
            }
            BASIC_LOGGER.debug(msg, objStrs.toArray());
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 提供debug级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void debug(String msg, Throwable throwable) {
            BASIC_LOGGER.debug(msg, throwable);
    }

    /**
     * 提供info级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void info(String msg) {
        BASIC_LOGGER.info(msg);
    }

    public static void info(String msg, Object... objs) {
        try {
            List<String> objStrs = new ArrayList<>();
            for (Object obj: objs) {
                if(obj instanceof String){
                    objStrs.add((String)obj);
                }else{
                    objStrs.add(JSONUtils.obj2json(obj));
                }
            }
            BASIC_LOGGER.info(msg, objStrs.toArray());
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 提供info级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void info(String msg, Throwable throwable) {
        BASIC_LOGGER.info(msg, throwable);
    }

    /**
     * 提供warn级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void warn(String msg) {
        BASIC_LOGGER.warn(msg);
    }

    public static void warn(String msg, Object... objs) {
        try {
            List<String> objStrs = new ArrayList<>();
            for (Object obj: objs) {
                if(obj instanceof String){
                    objStrs.add((String)obj);
                }else{
                    objStrs.add(JSONUtils.obj2json(obj));
                }
            }
            BASIC_LOGGER.warn(msg, objStrs.toArray());
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 提供warn级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void warn(String msg, Throwable throwable) {
        BASIC_LOGGER.warn(msg, throwable);
    }

    /**
     * 提供error级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void error(String msg) {
        BASIC_LOGGER.error(msg);
    }


    public static void error(String msg, Object... objs) {
        try {
            List<String> objStrs = new ArrayList<>();
            for (Object obj: objs) {
                if(obj instanceof String){
                    objStrs.add((String)obj);
                }else{
                    objStrs.add(JSONUtils.obj2json(obj));
                }
            }
            BASIC_LOGGER.error(msg, objStrs.toArray());
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * 提供error级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void error(String msg, Throwable throwable) {
        BASIC_LOGGER.error(msg, throwable);
    }

    public static  void error(Throwable throwable) {
        BASIC_LOGGER.error(throwable);
    }

    /**
     * 提供trace级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void trace(String msg) {
        BASIC_LOGGER.trace(msg);
    }

    /**
     * 提供trace级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void trace(String msg, Throwable throwable) {
        BASIC_LOGGER.trace(msg, throwable);
    }
}
