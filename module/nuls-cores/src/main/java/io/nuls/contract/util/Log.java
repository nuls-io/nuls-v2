package io.nuls.contract.util;

import io.nuls.contract.constant.ContractConstant;
import io.nuls.core.io.IoUtils;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.parse.JSONUtils;

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
     * Instantiation of this class is not allowed
     */
    private Log() {
    }

    /**
     * providedebugBasic level log output
     *
     * @param msg Message to be displayed
     */
    public static void debug(String msg) {
        getBasicLogger().debug(wrapperLogContent(msg));
    }

    public static void debug(String msg, Object... objs) {
        getBasicLogger().debug(wrapperLogContent(msg), objs);
    }

    /**
     * providedebugBasic level log output
     *
     * @param msg       Message to be displayed
     * @param throwable Abnormal information
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
     * provideinfoBasic level log output
     *
     * @param msg Message to be displayed
     */
    public static void info(String msg) {
        getBasicLogger().info(wrapperLogContent(msg));
    }

    public static void info(String msg, Object... objs) {
        getBasicLogger().info(wrapperLogContent(msg), objs);
    }

    /**
     * provideinfoBasic level log output
     *
     * @param msg       Message to be displayed
     * @param throwable Abnormal information
     */
    public static void info(String msg, Throwable throwable) {
        getBasicLogger().info(wrapperLogContent(msg), throwable);
    }

    /**
     * providewarnBasic level log output
     *
     * @param msg Message to be displayed
     */
    public static void warn(String msg) {
        getBasicLogger().warn(wrapperLogContent(msg));
    }

    public static void warn(String msg, Object... objs) {
        getBasicLogger().warn(wrapperLogContent(msg), objs);
    }

    /**
     * providewarnBasic level log output
     *
     * @param msg       Message to be displayed
     * @param throwable Abnormal information
     */
    public static void warn(String msg, Throwable throwable) {
        getBasicLogger().warn(wrapperLogContent(msg), throwable);
    }

    /**
     * provideerrorBasic level log output
     *
     * @param msg Message to be displayed
     */
    public static void error(String msg) {
        getBasicLogger().error(wrapperLogContent(msg));
    }


    public static void error(String msg, Object... objs) {
        getBasicLogger().error(wrapperLogContent(msg), objs);
    }

    /**
     * provideerrorBasic level log output
     *
     * @param msg       Message to be displayed
     * @param throwable Abnormal information
     */
    public static void error(String msg, Throwable throwable) {
        getBasicLogger().error(wrapperLogContent(msg), throwable);
    }

    public static void error(Throwable throwable) {
        getBasicLogger().error(throwable);
    }

    /**
     * providetraceBasic level log output
     *
     * @param msg Message to be displayed
     */
    public static void trace(String msg) {
        getBasicLogger().trace(wrapperLogContent(msg));
    }

    /**
     * providetraceBasic level log output
     *
     * @param msg       Message to be displayed
     * @param throwable Abnormal information
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
                Map<String,Object> json = JSONUtils.json2map(str);
                LogUtil.configDefaultLog(ContractConstant.LOG_FILE_NAME,
                        (String) json.get("packageLogPackages"), (String) json.get("packageLogLevels"));
            } catch (Exception e) {
                LogUtil.configDefaultLog(ContractConstant.LOG_FILE_NAME);
            }
        }

        return DEFAULT_BASIC_LOGGER;
    }

}
