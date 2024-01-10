package io.nuls.core.log;

import io.nuls.core.log.logback.LoggerBuilder;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.parse.JSONUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Public log class
 * Public Log Class
 *
 * @author tag
 * 2018/12/18
 * */
public class Log {

    public static final String BASIC_NAME = "common";

    public static NulsLogger BASIC_LOGGER = LoggerBuilder.getLogger(BASIC_NAME);

    /**
     * providedebugBasic level log output
     *
     * @param msg Message to be displayed
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
     * providedebugBasic level log output
     *
     * @param msg       Message to be displayed
     * @param throwable Abnormal information
     */
    public static void debug(String msg, Throwable throwable) {
            BASIC_LOGGER.debug(msg, throwable);
    }

    /**
     * provideinfoBasic level log output
     *
     * @param msg Message to be displayed
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
     * provideinfoBasic level log output
     *
     * @param msg       Message to be displayed
     * @param throwable Abnormal information
     */
    public static void info(String msg, Throwable throwable) {
        BASIC_LOGGER.info(msg, throwable);
    }

    /**
     * providewarnBasic level log output
     *
     * @param msg Message to be displayed
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
     * providewarnBasic level log output
     *
     * @param msg       Message to be displayed
     * @param throwable Abnormal information
     */
    public static void warn(String msg, Throwable throwable) {
        BASIC_LOGGER.warn(msg, throwable);
    }

    public static void warn(Throwable throwable) {
        BASIC_LOGGER.warn("", throwable);
    }

    /**
     * provideerrorBasic level log output
     *
     * @param msg Message to be displayed
     */
    public static void error(String msg) {
        BASIC_LOGGER.error(msg);
    }


    public static void error(String msg, Object... objs) {
        try {
            List<Object> objStrs = new ArrayList<>();
            for (Object obj : objs) {
                if (obj instanceof String) {
                    objStrs.add(obj);
                } else if (obj instanceof Throwable) {
                    objStrs.add(obj);
                } else {
                    objStrs.add(JSONUtils.obj2json(obj));
                }
            }
            BASIC_LOGGER.error(msg, objStrs.toArray());
        }catch (Exception e){
            Log.error(e);
        }
    }

    /**
     * provideerrorBasic level log output
     *
     * @param msg       Message to be displayed
     * @param throwable Abnormal information
     */
    public static void error(String msg, Throwable throwable) {
        BASIC_LOGGER.error(msg, throwable);
    }

    public static  void error(Throwable throwable) {
        BASIC_LOGGER.error(throwable);
    }

    /**
     * providetraceBasic level log output
     *
     * @param msg Message to be displayed
     */
    public static void trace(String msg) {
        BASIC_LOGGER.trace(msg);
    }

    /**
     * providetraceBasic level log output
     *
     * @param msg       Message to be displayed
     * @param throwable Abnormal information
     */
    public static void trace(String msg, Throwable throwable) {
        BASIC_LOGGER.trace(msg, throwable);
    }
}
