/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.rpc.info;

import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebsocketTool的常量
 * Constants of WebsocketTool
 *
 * @author tangyi
 * @date 2018/10/19
 */
public class Constants {

    /**
     * 不允许实例化
     * Instantiation is not allowed
     */
    private Constants() {
    }

    /**
     * 1秒 = 1000毫秒
     * One second = 1000 milliseconds
     */
    public static final long MILLIS_PER_SECOND = 1000L;

    /**
     * 循环暂停时间
     * Loop pause time
     */
    public static final long INTERVAL_TIMEMILLIS = 10L;

    /**
     * 超时毫秒数(1分钟)
     * Timeout millisecond(1 minute)
     */
    public static final long TIMEOUT_TIMEMILLIS = 60L * MILLIS_PER_SECOND;

    /**
     * 取消订阅的常量，已经无用
     * Constants for unsubscribed, it's useless.
     */
    @Deprecated
    public static final long UNSUBSCRIBE_TIMEMILLIS = -20140217L;

    /**
     * 调用远程方法时，用以传输版本号的参数名
     * When calling a remote method, the parameter name used to transfer the version number
     */
    public static final String VERSION_KEY_STR = "version";

    /**
     * 用以保存IP地址的参数名
     * The parameter name used to save the IP address
     */
    public static final String KEY_IP = "IP";

    /**
     * 用以保存端口的参数名
     * The parameter name used to save the port
     */
    public static final String KEY_PORT = "Port";

    /**
     * 接口权限级别
     * 第三方应用/平台也能调用的公开接口
     * <p>
     * Interface permission level
     * A public interface that third-party applications/platforms can call
     */
    public static final String PUBLIC = "public";

    /**
     * 接口权限级别
     * 只有模块间内部才能调用的接口
     * <p>
     * Interface permission level
     * An interface that can only be invoked internally between modules
     */
    public static final String PRIVATE = "private";

    /**
     * 接口权限级别
     * 专门为管理员设计的特定接口.
     * <p>
     * Interface permission level
     * A Specific Interface Designed for Administrators
     */
    public static final String ADMIN = "admin";


    /**
     * 收到Request请求后，根据属性判断如何执行
     * 1：执行Request，并保留等待下次执行
     * <p>
     * After receiving the Request, determine how to execute it based on the attributes
     * 1: Execute Request and keep waiting for the next execution
     */
    public static final int EXECUTE_AND_KEEP = 1;

    /**
     * 收到Request请求后，根据属性判断如何执行
     * 2：执行Request，然后丢弃
     * <p>
     * After receiving the Request, determine how to execute it based on the attributes
     * 2: Execute Request and discard it
     */
    public static final int EXECUTE_AND_REMOVE = 2;

    /**
     * 收到Request请求后，根据属性判断如何执行
     * 3：不执行Request，但是保留等待下次执行
     * <p>
     * After receiving the Request, determine how to execute it based on the attributes
     * 3: Do not execute Request, but keep waiting for the next execution
     */
    public static final int SKIP_AND_KEEP = 3;

    /**
     * 收到Request请求后，根据属性判断如何执行
     * 4：不执行Request，并且丢弃
     * <p>
     * After receiving the Request, determine how to execute it based on the attributes
     * 4: Do not execute Request, and discard it
     */
    public static final int SKIP_AND_REMOVE = 4;





    /**
     * 用以生成messageId，自增长，模块内唯一
     * Used to generate message Id, self-growing, unique within the module
     */
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);


    /**
     * NULS2.0中的标准，用1代表true
     * Standard in NULS 2.0, 1 for true
     */
    public static final String BOOLEAN_TRUE = "1";
    /**
     * NULS2.0中的标准，用0代表false
     * Standard in NULS 2.0, 0 for false
     */
    public static final String BOOLEAN_FALSE = "0";

    public static final String ZERO = "0";

    /**
     * 处理待处理消息的线程池
     * Thread pool for processing messages to be processed
     */
    public static final ExecutorService THREAD_POOL = ThreadUtils.createThreadPool(5, 500, new NulsThreadFactory("Processor"));

    public static final String RESPONSE_TIMEOUT = "Response timeout";
    public static final String CMD_NOT_FOUND = "Cmd not found";
    public static final String CMD_DUPLICATE = "Duplicate cmd found";
    public static final String RANGE_REGEX = "[(\\[]\\d+,\\d+[)\\]]";
    public static final String PARAM_WRONG_RANGE = "Param wrong range";
    public static final String PARAM_WRONG_FORMAT = "Param wrong format";
    public static final String PARAM_NULL = "Param null";


    /**
     * 获取下一个messageId
     * Get the next messageId
     */
    public static String nextSequence() {
        return SEQUENCE.incrementAndGet() + "";
    }
}
