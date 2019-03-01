package io.nuls.tools.log;

import ch.qos.logback.classic.Level;
import io.nuls.tools.log.logback.LoggerBuilder;
import io.nuls.tools.log.logback.NulsLogger;

/**
 * 公共日志类
 * Public Log Class
 *
 * @author tag
 * 2018/12/18
 * */
public class Log {
    private static final String BASIC_NAME = "common/nuls";
    private static final NulsLogger BASIC_LOGGER = LoggerBuilder.getLogger(BASIC_NAME, Level.DEBUG);

    /**
     * 提供debug级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void debug(String msg) {
        BASIC_LOGGER.debug(msg);
    }

    public static void debug(String msg, Object... objs) {
            BASIC_LOGGER.debug(msg, objs);
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
        BASIC_LOGGER.info(msg, objs);
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
        BASIC_LOGGER.warn(msg, objs);
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
        BASIC_LOGGER.error(msg, objs);
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
