package io.nuls.contract.util;

import ch.qos.logback.classic.Level;
import io.nuls.tools.log.logback.LoggerBuilder;
import io.nuls.tools.log.logback.NulsLogger;

/**
 * @author: PierreLuo
 * @date: 2019-03-23
 */
public class Log {

    private static final NulsLogger BASIC_LOGGER;

    static {
        BASIC_LOGGER = LoggerBuilder.getLogger("./contract", "contract", Level.INFO, Level.INFO);
        BASIC_LOGGER.addBasicPath(Log.class.getName());
    }

    /**
     * 不允许实例化该类
     */
    private Log() {
    }

    /**
     * 提供debug级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void debug(String msg) {
        BASIC_LOGGER.debug(wrapperLogContent(msg));
    }

    public static void debug(String msg, Object... objs) {
        BASIC_LOGGER.debug(wrapperLogContent(msg), objs);
    }

    /**
     * 提供debug级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void debug(String msg, Throwable throwable) {
        BASIC_LOGGER.debug(wrapperLogContent(msg), throwable);
    }

    public static boolean isTraceEnabled() {
        return BASIC_LOGGER.getLogger().isTraceEnabled();
    }

    public static boolean isDebugEnabled() {
        return BASIC_LOGGER.getLogger().isDebugEnabled();
    }

    public static boolean isInfoEnabled() {
        return BASIC_LOGGER.getLogger().isInfoEnabled();
    }

    public static boolean isWarnEnabled() {
        return BASIC_LOGGER.getLogger().isWarnEnabled();
    }

    public static boolean isErrorEnabled() {
        return BASIC_LOGGER.getLogger().isErrorEnabled();
    }

    /**
     * 提供info级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void info(String msg) {
        BASIC_LOGGER.info(wrapperLogContent(msg));
    }

    public static void info(String msg, Object... objs) {
        BASIC_LOGGER.info(wrapperLogContent(msg), objs);
    }

    /**
     * 提供info级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void info(String msg, Throwable throwable) {
        BASIC_LOGGER.info(wrapperLogContent(msg), throwable);
    }

    /**
     * 提供warn级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void warn(String msg) {
        BASIC_LOGGER.warn(wrapperLogContent(msg));
    }

    public static void warn(String msg, Object... objs) {
        BASIC_LOGGER.warn(wrapperLogContent(msg), objs);
    }

    /**
     * 提供warn级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void warn(String msg, Throwable throwable) {
        BASIC_LOGGER.warn(wrapperLogContent(msg), throwable);
    }

    /**
     * 提供error级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void error(String msg) {
        BASIC_LOGGER.error(wrapperLogContent(msg));
    }


    public static void error(String msg, Object... objs) {
        BASIC_LOGGER.error(wrapperLogContent(msg), objs);
    }

    /**
     * 提供error级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void error(String msg, Throwable throwable) {
        BASIC_LOGGER.error(wrapperLogContent(msg), throwable);
    }

    public static void error(Throwable throwable) {
        BASIC_LOGGER.error(throwable);
    }

    /**
     * 提供trace级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void trace(String msg) {
        BASIC_LOGGER.trace(wrapperLogContent(msg));
    }

    /**
     * 提供trace级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void trace(String msg, Throwable throwable) {
        BASIC_LOGGER.trace(wrapperLogContent(msg), throwable);
    }

    private static String wrapperLogContent(String msg) {
        return msg;
    }
}