package io.nuls.contract.util;

import ch.qos.logback.classic.Level;
import com.alibaba.fastjson.JSONObject;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.core.io.IoUtils;
import io.nuls.core.log.logback.NulsLogger;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static io.nuls.contract.constant.ContractConstant.MODULE_CONFIG_FILE;

/**
 * @author: PierreLuo
 * @date: 2019-03-23
 */
public class Log {

    public static NulsLogger DEFAULT_BASIC_LOGGER;

    private static ThreadLocal<Integer> currentThreadChainId = new ThreadLocal<>();

    public static Map<Integer, NulsLogger> BASIC_LOGGER_MAP = new HashMap<>(4);

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
        getBasicLogger().debug(wrapperLogContent(msg));
    }

    public static void debug(String msg, Object... objs) {
        getBasicLogger().debug(wrapperLogContent(msg), objs);
    }

    /**
     * 提供debug级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void debug(String msg, Throwable throwable) {
        getBasicLogger().debug(wrapperLogContent(msg), throwable);
    }

    public static boolean isTraceEnabled() {
        return getBasicLogger().getLogger().isTraceEnabled();
    }

    public static boolean isDebugEnabled() {
        return getBasicLogger().getLogger().isDebugEnabled();
    }

    public static boolean isInfoEnabled() {
        return getBasicLogger().getLogger().isInfoEnabled();
    }

    public static boolean isWarnEnabled() {
        return getBasicLogger().getLogger().isWarnEnabled();
    }

    public static boolean isErrorEnabled() {
        return getBasicLogger().getLogger().isErrorEnabled();
    }

    /**
     * 提供info级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void info(String msg) {
        getBasicLogger().info(wrapperLogContent(msg));
    }

    public static void info(String msg, Object... objs) {
        getBasicLogger().info(wrapperLogContent(msg), objs);
    }

    /**
     * 提供info级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void info(String msg, Throwable throwable) {
        getBasicLogger().info(wrapperLogContent(msg), throwable);
    }

    /**
     * 提供warn级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void warn(String msg) {
        getBasicLogger().warn(wrapperLogContent(msg));
    }

    public static void warn(String msg, Object... objs) {
        getBasicLogger().warn(wrapperLogContent(msg), objs);
    }

    /**
     * 提供warn级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void warn(String msg, Throwable throwable) {
        getBasicLogger().warn(wrapperLogContent(msg), throwable);
    }

    /**
     * 提供error级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void error(String msg) {
        getBasicLogger().error(wrapperLogContent(msg));
    }


    public static void error(String msg, Object... objs) {
        getBasicLogger().error(wrapperLogContent(msg), objs);
    }

    /**
     * 提供error级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void error(String msg, Throwable throwable) {
        getBasicLogger().error(wrapperLogContent(msg), throwable);
    }

    public static void error(Throwable throwable) {
        getBasicLogger().error(throwable);
    }

    /**
     * 提供trace级别基本的日志输出
     *
     * @param msg 需要显示的消息
     */
    public static void trace(String msg) {
        getBasicLogger().trace(wrapperLogContent(msg));
    }

    /**
     * 提供trace级别基本的日志输出
     *
     * @param msg       需要显示的消息
     * @param throwable 异常信息
     */
    public static void trace(String msg, Throwable throwable) {
        getBasicLogger().trace(wrapperLogContent(msg), throwable);
    }

    private static String wrapperLogContent(String msg) {
        return msg;
    }

    public static void currentThreadChainId(Integer chainId) {
        currentThreadChainId.set(chainId);
    }

    private static NulsLogger getBasicLogger() {
        Integer chainId = currentThreadChainId.get();
        if(chainId != null) {
            NulsLogger nulsLogger = BASIC_LOGGER_MAP.get(chainId);
            if(nulsLogger != null) {
                return nulsLogger;
            }
            return getDefaultBasicLogger();
        } else {
            return getDefaultBasicLogger();
        }
    }

    private static NulsLogger getDefaultBasicLogger() {
        if(DEFAULT_BASIC_LOGGER == null) {
            try (InputStream configInput = Log.class.getClassLoader().getResourceAsStream(MODULE_CONFIG_FILE)) {
                String str = IoUtils.readBytesToString(configInput);
                JSONObject json = JSONObject.parseObject(str);
                LogUtil.configDefaultLog(ContractConstant.LOG_FILE_FOLDER, ContractConstant.LOG_FILE_NAME,
                        Level.toLevel(json.getString("logFileLevel")), Level.toLevel(json.getString("logConsoleLevel")),
                        json.getString("systemLogLevel"), json.getString("packageLogPackages"), json.getString("packageLogLevels"));
            } catch (Exception e) {
                LogUtil.configDefaultLog("./contract", "contract", Level.INFO, Level.INFO);
            }
        }

        return DEFAULT_BASIC_LOGGER;
    }

}