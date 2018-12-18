package io.nuls.tools.log.logback;

import ch.qos.logback.classic.Logger;

/**
 * 系统日志类
 * System Log Class
 *
 * @author tag
 * 2018/12/18
 * */
public class NulsLogger {
    private String BASIC_PATH = "io.nuls.tools.log.Log";
    private Logger logger;

    public NulsLogger(Logger logger){
        this.logger = logger;
    }

    /**
     * 提供debug级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public void debug(String msg) {
        if (logger.isDebugEnabled()) {
            String logContent = getLogTrace() + ":" + msg;
            logger.debug(logContent);
        }
    }

    public void debug(String msg, Object... objs) {
        if (logger.isDebugEnabled()) {
            String logContent = getLogTrace() + ":" + msg;
            logger.debug(logContent, objs);
        }
    }

    /**
     * 提供debug级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public void debug(String msg, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            String logContent = getLogTrace() + ":" + msg;
            logger.debug(logContent, throwable);
        }
    }

    /**
     * 提供info级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public void info(String msg) {
        String logContent = getLogTrace() + ":" + msg;
        logger.info(logContent);
    }

    public void info(String msg, Object... objs) {
        String logContent = getLogTrace() + ":" + msg;
        logger.info(logContent, objs);
    }

    /**
     * 提供info级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public void info(String msg, Throwable throwable) {
        String logContent = getLogTrace() + ":" + msg;
        logger.info(logContent, throwable);
    }

    /**
     * 提供warn级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public void warn(String msg) {
        String logContent = getLogTrace() + ":" + msg;
        logger.warn(logContent);
    }

    public void warn(String msg, Object... objs) {
        String logContent = getLogTrace() + ":" + msg;
        logger.warn(logContent, objs);
    }

    /**
     * 提供warn级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public void warn(String msg, Throwable throwable) {
        String logContent = getLogTrace() + ":" + msg;
        logger.warn(logContent, throwable);
    }

    /**
     * 提供error级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public void error(String msg) {
        String logContent = getLogTrace() + ":" + msg;
        logger.error(logContent);
    }


    public void error(String msg, Object... objs) {
        String logContent = getLogTrace() + ":" + msg;
        logger.error(logContent, objs);
    }

    /**
     * 提供error级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public void error(String msg, Throwable throwable) {
        String logContent = getLogTrace() + ":" + msg;
        logger.error(logContent, throwable);
    }

    public  void error(Throwable throwable) {
        String logContent = getLogTrace() + ":" ;
        logger.error(logContent,throwable);
    }

    /**
     * 提供trace级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public  void trace(String msg) {
        String logContent = getLogTrace() + ":" + msg;
        logger.trace(logContent);
    }

    /**
     * 提供trace级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public  void trace(String msg, Throwable throwable) {
        String logContent = getLogTrace() + ":" + msg;
        logger.trace(logContent, throwable);
    }

    /**
     * 获取日志记录点的全路径
     *
     * @return 日志记录点的全路径
     */
    private String getLogTrace() {
        StringBuilder logTrace = new StringBuilder();
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length > 1) {
            // index为3上一级调用的堆栈信息，index为1和2都为Log类自己调两次（可忽略），index为0为主线程触发（可忽略）
            StackTraceElement ste = stack[3];
            if(BASIC_PATH.equals(ste.getClassName())){
                ste = stack[4];
            }
            if (ste != null) {
                // 获取类名、方法名、日志的代码行数
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
}
