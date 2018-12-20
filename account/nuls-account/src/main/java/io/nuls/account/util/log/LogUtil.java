package io.nuls.account.util.log;

import io.nuls.tools.log.logback.LoggerBuilder;
import io.nuls.tools.log.logback.NulsLogger;

/**
 * 账户模块日志类
 * Account module LogUtil Class
 *
 * @author tag
 * 2018/12/18
 */
public class LogUtil {
    private static final String FOLDER_NAME = "account";
    private static final String FILE_NAME = "account";
    private static final NulsLogger LOGGER = LoggerBuilder.getLogger(FOLDER_NAME, FILE_NAME);

    /**
     * 提供debug级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void debug(String msg) {
        LOGGER.debug(msg);
    }

    public static void debug(String msg, Object... objs) {
        LOGGER.debug(msg, objs);
    }

    /**
     * 提供debug级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void debug(String msg, Throwable throwable) {
        LOGGER.debug(msg, throwable);
    }

    /**
     * 提供info级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void info(String msg) {
        LOGGER.info(msg);
    }

    public static void info(String msg, Object... objs) {
        LOGGER.info(msg, objs);
    }

    /**
     * 提供info级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void info(String msg, Throwable throwable) {
        LOGGER.info(msg, throwable);
    }

    /**
     * 提供warn级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void warn(String msg) {
        LOGGER.warn(msg);
    }

    public static void warn(String msg, Object... objs) {
        LOGGER.warn(msg, objs);
    }

    /**
     * 提供warn级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void warn(String msg, Throwable throwable) {
        LOGGER.warn(msg, throwable);
    }

    /**
     * 提供error级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void error(String msg) {
        LOGGER.error(msg);
    }


    public static void error(String msg, Object... objs) {
        LOGGER.error(msg, objs);
    }

    /**
     * 提供error级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void error(String msg, Throwable throwable) {
        LOGGER.error(msg, throwable);
    }

    public static void error(Throwable throwable) {
        LOGGER.error("", throwable);
    }

}
