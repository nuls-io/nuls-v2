package io.nuls.core.log.logback;

import ch.qos.logback.classic.Logger;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * System log class
 * System Log Class
 *
 * @author tag
 * 2018/12/18
 * */
public class NulsLogger {
    private Set<String> BASIC_PATH_MAP = new HashSet<>();
    private String BASIC_PATH = Log.class.getName();
    private Logger logger;

    public NulsLogger(Logger logger){
        this.logger = logger;
        BASIC_PATH_MAP.add(BASIC_PATH);
    }

    /**
     * providedebugBasic level log output
     *
     * @param msg Message to be displayed
     */
    public void debug(String msg) {
        if(logger.isDebugEnabled()){
            String logContent = getLogTrace() + ":" + msg;
            logger.debug(logContent);
        }
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void debug(String msg, Object... objs) {
        if(logger.isDebugEnabled()){
            String logContent = getLogTrace() + ":" + msg;
            try {
                List<String> objStrs = new ArrayList<>();
                for (Object obj: objs) {
                    if(obj instanceof String){
                        objStrs.add((String)obj);
                    }else{
                        objStrs.add(JSONUtils.obj2json(obj));
                    }
                }
                logger.debug(logContent, objStrs.toArray());
            }catch (Exception e){
                Log.error(e);
            }
        }
    }

    /**
     * providedebugBasic level log output
     *
     * @param msg       Message to be displayed
     * @param throwable Abnormal information
     */
    public void debug(String msg, Throwable throwable) {
        if(logger.isDebugEnabled()) {
            String logContent = getLogTrace() + ":" + msg;
            logger.debug(logContent, throwable);
        }
    }

    /**
     * provideinfoBasic level log output
     *
     * @param msg Message to be displayed
     */
    public void info(String msg) {
        String logContent = getLogTrace() + ":" + msg;
        logger.info(logContent);
    }

    public void info(String msg, Object... objs) {
        String logContent = getLogTrace() + ":" + msg;
        try {
            List<String> objStrs = new ArrayList<>();
            for (Object obj: objs) {
                if(obj instanceof String){
                    objStrs.add((String)obj);
                }else{
                    objStrs.add(JSONUtils.obj2json(obj));
                }
            }
            logger.info(logContent, objStrs.toArray());
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
    public void info(String msg, Throwable throwable) {
        String logContent = getLogTrace() + ":" + msg;
        logger.info(logContent, throwable);
    }

    /**
     * providewarnBasic level log output
     *
     * @param msg Message to be displayed
     */
    public void warn(String msg) {
        String logContent = getLogTrace() + ":" + msg;
        logger.warn(logContent);
    }

    public void warn(String msg, Object... objs) {
        String logContent = getLogTrace() + ":" + msg;
        try {
            List<String> objStrs = new ArrayList<>();
            for (Object obj: objs) {
                if(obj instanceof String){
                    objStrs.add((String)obj);
                }else{
                    objStrs.add(JSONUtils.obj2json(obj));
                }
            }
            logger.warn(logContent, objStrs.toArray());
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
    public void warn(String msg, Throwable throwable) {
        String logContent = getLogTrace() + ":" + msg;
        logger.warn(logContent, throwable);
    }

    /**
     * provideerrorBasic level log output
     *
     * @param msg Message to be displayed
     */
    public void error(String msg) {
        String logContent = getLogTrace() + ":" + msg;
        logger.error(logContent);
    }


    public void error(String msg, Object... objs) {
        String logContent = getLogTrace() + ":" + msg;
        try {
            List<Object> objStrs = new ArrayList<>();
            for (Object obj : objs) {
                if (obj instanceof String) {
                    objStrs.add((String) obj);
                } else if (obj instanceof Throwable) {
                    objStrs.add(obj);
                } else {
                    objStrs.add(JSONUtils.obj2json(obj));
                }
            }
            logger.error(logContent, objStrs.toArray());
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
    public void error(String msg, Throwable throwable) {
        String logContent = getLogTrace() + ":" + msg;
        logger.error(logContent, throwable);
    }

    public void error(Throwable throwable) {
        String logContent = getLogTrace() + ":" ;
        logger.error(logContent,throwable);
    }

    public void error(String msg, Exception e) {
        String logContent = getLogTrace() + ":" + msg;
        if(e instanceof NulsRuntimeException){
            logger.error(logContent + ":" + ((NulsRuntimeException)e).format(), e);
        } else if(e instanceof NulsException){
            logger.error(logContent + ":" + ((NulsException)e).format(), e);
        }else {
            logger.error(logContent, e);
        }
    }

    public void error(Exception e) {
        if(e instanceof NulsRuntimeException){
            error((NulsRuntimeException) e);
        } else if(e instanceof NulsException){
            error((NulsException) e);
        } else {
            String logContent = getLogTrace() + ":" ;
            logger.error(logContent, e);
        }
    }

    public void error(NulsRuntimeException e) {
        String logContent = getLogTrace() + ":" + e.format();
        logger.error(logContent, e);
    }
    public void error(NulsException e) {
        String logContent = getLogTrace() + ":" + e.format() ;
        logger.error(logContent, e);
    }

    /**
     * providetraceBasic level log output
     *
     * @param msg Message to be displayed
     */
    public  void trace(String msg) {
        String logContent = getLogTrace() + ":" + msg;
        logger.trace(logContent);
    }

    /**
     * providetraceBasic level log output
     *
     * @param msg       Message to be displayed
     * @param throwable Abnormal information
     */
    public  void trace(String msg, Throwable throwable) {
        String logContent = getLogTrace() + ":" + msg;
        logger.trace(logContent, throwable);
    }

    /**
     * Obtain the full path of the logging point
     *
     * @return The full path of the logging point
     */
    private String getLogTrace() {
        StringBuilder logTrace = new StringBuilder();
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length > 1) {
            // indexby3The stack information of the previous level call,indexby1and2All forLogClass adjusts itself twice（Negligible）,indexby0Triggered by the main thread（Negligible）
            StackTraceElement ste = stack[3];
            if(BASIC_PATH_MAP.contains(ste.getClassName())){
                ste = stack[4];
            }
            if (ste != null) {
                // Get class name、Method name、Number of code lines in the log
                logTrace.append(ste.getClassName());
                logTrace.append('.');
                logTrace.append(ste.getMethodName());
                logTrace.append('(');
                logTrace.append(ste.getFileName());
                logTrace.append(':');
                logTrace.append(ste.getLineNumber());
                logTrace.append(')');
            }
        }
        return logTrace.toString();
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void addBasicPath(String basicPath) {
        BASIC_PATH_MAP.add(basicPath);
    }
}
