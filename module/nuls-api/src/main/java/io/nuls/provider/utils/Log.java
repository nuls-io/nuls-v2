/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.provider.utils;

import io.nuls.core.log.logback.LoggerBuilder;
import io.nuls.core.log.logback.NulsLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-06-27
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
            DEFAULT_BASIC_LOGGER = LoggerBuilder.getLogger("sdk-provider");
            DEFAULT_BASIC_LOGGER.addBasicPath(Log.class.getName());
        }
        return DEFAULT_BASIC_LOGGER;
    }
}
