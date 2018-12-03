/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *  *
 *
 */

package io.nuls.rpc.info;

import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.NegotiateConnection;
import io.nuls.tools.data.DateUtils;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.TimeService;
import io.nuls.tools.thread.commom.NulsThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebsocketTool的常量
 * Constant of WebsocketTool
 *
 * @author tangyi
 * @date 2018/10/19
 * @description
 */
public class Constants {

    private Constants() {
    }

    /**
     * 1秒 = 1000毫秒
     * One second = 1000 milliseconds
     */
    public static final long MILLIS_PER_SECOND = 1000;

    /**
     * 循环暂停时间
     * Loop pause time
     */
    public static final long INTERVAL_TIMEMILLIS = 10L;

    /**
     * 超时毫秒数
     * Timeout millisecond
     */
    public static final long TIMEOUT_TIMEMILLIS = 60 * 1000L;

    /**
     * 取消订阅的常量，为什么是它？仅仅是一个彩蛋
     * Constants for unsubscribed, why is it? Just a colored egg
     */
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
     * PUBLIC：第三方应用/平台也能调用的公开接口
     * PRIVATE：只有模块间内部才能调用的接口
     * ADMIN：专门为管理员设计的特定接口
     * Interface permission level
     * PUBLIC: A public interface that third-party applications/platforms can call
     * PRIVATE: An interface that can only be invoked internally between modules
     * ADMIN: A Specific Interface Designed for Administrators
     */
    public static final String PUBLIC = "public";
    public static final String PRIVATE = "private";
    public static final String ADMIN = "admin";


    /**
     * 收到Request请求后，根据属性判断如何执行
     * 1：执行Request，并保留等待下次执行
     * 2：执行Request，然后丢弃
     * 3：不执行Request，但是保留等待下次执行
     * 4：不执行Request，并且丢弃
     * After receiving the Request, determine how to execute it based on the attributes
     * 1: Execute Request and keep waiting for the next execution
     * 2: Execute Request and discard it
     * 3: Do not execute Request, but keep waiting for the next execution
     * 4: Do not execute Request, and discard it
     */
    public static final int INVOKE_EXECUTE_KEEP = 1;
    public static final int INVOKE_EXECUTE_REMOVE = 2;
    public static final int INVOKE_SKIP_KEEP = 3;
    public static final int INVOKE_SKIP_REMOVE = 4;


    /**
     * 预定义的请求错误信息
     * Predefined error message of the request
     */
    public static final String RESPONSE_TIMEOUT = "Response timeout";
    public static final String CMD_NOT_FOUND = "Cmd not found";
    public static final String CMD_DUPLICATE = "Duplicate cmd found";
    public static final String RANGE_REGEX = "[(\\[]\\d+,\\d+[)\\]]";
    public static final String PARAM_WRONG_RANGE = "Param wrong range";
    public static final String PARAM_WRONG_FORMAT = "Param wrong format";
    public static final String PARAM_NULL = "Param null";


    /**
     * 用以生成messageId，自增长，模块内唯一
     * Used to generate message Id, self-growing, unique within the module
     */
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    /**
     * 获取下一个messageId
     * Get the next messageId
     */
    public static String nextSequence() {
        return SEQUENCE.incrementAndGet() + "";
    }


    /**
     * NULS2.0中的标准，用1代表true，0代表false
     * Standard in NULS 2.0, 1 for true and 0 for false
     */
    public static final String BOOLEAN_TRUE = "1";
    public static final String BOOLEAN_FALSE = "0";

    /**
     * 处理待处理消息的线程池
     * Thread pool for processing messages to be processed
     */
    public static final ExecutorService THREAD_POOL = ThreadUtils.createThreadPool(5, 500, new NulsThreadFactory("Processor"));

    /**
     * 根据bool类型生成对应字符串
     * Generate corresponding strings according to bool type
     */
    public static String booleanString(boolean bool) {
        return bool ? BOOLEAN_TRUE : BOOLEAN_FALSE;
    }

    /**
     * 核心模块（Manager）的连接地址
     * URL of Core Module (Manager)
     */
    public static String kernelUrl = "";

    /*
      我是华丽的分隔符
      I am a gorgeous separator
     */

    /**
     * 默认Message对象
     * Default Message object
     */
    public static Message basicMessage(String messageId, MessageType messageType) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setMessageType(messageType.name());
        message.setTimestamp(TimeService.currentTimeMillis() + "");
        message.setTimezone(DateUtils.getTimeZone() + "");
        return message;
    }

    /**
     * 默认握手对象
     * Default NegotiateConnection object
     */
    public static NegotiateConnection defaultNegotiateConnection() {
        NegotiateConnection negotiateConnection = new NegotiateConnection();
        negotiateConnection.setProtocolVersion("1.0");
        negotiateConnection.setCompressionAlgorithm("zlib");
        negotiateConnection.setCompressionRate("0");
        return negotiateConnection;
    }


}
