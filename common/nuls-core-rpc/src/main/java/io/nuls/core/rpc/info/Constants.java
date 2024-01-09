/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.core.rpc.info;

import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebsocketToolConstant of
 * Constants of WebsocketTool
 *
 * @author tangyi
 * @date 2018/10/19
 */
public class Constants {

    /**
     * Instantiation not allowed
     * Instantiation is not allowed
     */
    private Constants() {
    }

    /**
     * 1second = 1000millisecond
     * One second = 1000 milliseconds
     */
    public static final long MILLIS_PER_SECOND = 1000L;

    /**
     * Loop pause time
     * Loop pause time
     */
    public static final long INTERVAL_TIMEMILLIS = 1L;

    /**
     * Timed task processor cycle interval time
     * Timed task processor cycle interval
     */
    public static final long PROCESSOR_INTERVAL_TIMEMILLIS = 6L * MILLIS_PER_SECOND;

    /**
     * Timed out milliseconds(1minute)
     * Timeout millisecond(1 minute)
     */
    public static final long TIMEOUT_TIMEMILLIS = 10L * MILLIS_PER_SECOND;

    /**
     * The constant for unsubscribing is no longer useful
     * Constants for unsubscribed, it's useless.
     */
    @Deprecated
    public static final long UNSUBSCRIBE_TIMEMILLIS = -20140217L;

    /**
     * The parameter name used to transfer the version number when calling a remote method
     * When calling a remote method, the parameter name used to transfer the version number
     */
    public static final String VERSION_KEY_STR = "version";
    /**
     * When calling remote methods, used to transport the chainID
     * When calling a remote method, the parameter name used to transfer the chain id
     */
    public static final String CHAIN_ID = "chainId";

    /**
     * Used to saveIPThe parameter name of the address
     * The parameter name used to save the IP address
     */
    public static final String KEY_IP = "IP";

    /**
     * The parameter name used to save the port
     * The parameter name used to save the port
     */
    public static final String KEY_PORT = "Port";

    /**
     * Interface permission level
     * Third party applications/Public interfaces that can also be called by the platform
     * <p>
     * Interface permission level
     * A public interface that third-party applications/platforms can call
     */
    public static final String PUBLIC = "public";

    /**
     * Interface permission level
     * An interface that can only be called internally between modules
     * <p>
     * Interface permission level
     * An interface that can only be invoked internally between modules
     */
    public static final String PRIVATE = "private";

    /**
     * Interface permission level
     * A specific interface designed specifically for administrators.
     * <p>
     * Interface permission level
     * A Specific Interface Designed for Administrators
     */
    public static final String ADMIN = "admin";


    /**
     * receiveRequestAfter the request, determine how to execute based on the attributes
     * 1：implementRequest, and keep it waiting for the next execution
     * <p>
     * After receiving the Request, determine how to execute it based on the attributes
     * 1: Execute Request and keep waiting for the next execution
     */
    public static final int EXECUTE_AND_KEEP = 1;

    /**
     * receiveRequestAfter the request, determine how to execute based on the attributes
     * 2：implementRequest, and then discard it
     * <p>
     * After receiving the Request, determine how to execute it based on the attributes
     * 2: Execute Request and discard it
     */
    public static final int EXECUTE_AND_REMOVE = 2;

    /**
     * receiveRequestAfter the request, determine how to execute based on the attributes
     * 3：Do not executeRequest, but keep waiting for the next execution
     * <p>
     * After receiving the Request, determine how to execute it based on the attributes
     * 3: Do not execute Request, but keep waiting for the next execution
     */
    public static final int SKIP_AND_KEEP = 3;

    /**
     * receiveRequestAfter the request, determine how to execute based on the attributes
     * 4：Do not executeRequestAnd discard it
     * <p>
     * After receiving the Request, determine how to execute it based on the attributes
     * 4: Do not execute Request, and discard it
     */
    public static final int SKIP_AND_REMOVE = 4;

    /**
     * Used to generatemessageId, self growth, unique within the module
     * Used to generate message Id, self-growing, unique within the module
     */
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);


    /**
     * NULS2.0Using the standard in1representativetrue
     * Standard in NULS 2.0, 1 for true
     */
    public static final String BOOLEAN_TRUE = "1";
    /**
     * NULS2.0Using the standard in0representativefalse
     * Standard in NULS 2.0, 0 for false
     */
    public static final String BOOLEAN_FALSE = "0";



    public static final String ZERO = "0";

    /**
     * Thread pool for processing pending messages
     * Thread pool for processing messages to be processed
     */
    public static final ExecutorService THREAD_POOL = ThreadUtils.createThreadPool(3, 500, new NulsThreadFactory("Processor"));

    public static final String RESPONSE_TIMEOUT = "Response timeout";
    public static final String CMD_NOT_FOUND = "Cmd not found";
    public static final String CMD_DUPLICATE = "Duplicate cmd found";
    //public static final String RANGE_REGEX = "[(\\[]\\d+,\\d+[)\\]]";
    /**
     * Parameter validation regular expression
     * Parametric Verification Regular Expressions
     */
    public static final String RANGE_REGEX = "\\[(\\-|\\+)?\\d+(\\.\\d+)?,(\\-|\\+)?\\d+(\\.\\d+)?\\]";
    public static final String PARAM_WRONG_RANGE = "Param wrong range";
    public static final String PARAM_WRONG_FORMAT = "Param wrong format";
    public static final String PARAM_NULL = "Param null";


    /**
     * Get NextmessageId
     * Get the next messageId
     */
    public static String nextSequence() {
        return System.currentTimeMillis()+""+SEQUENCE.incrementAndGet();
    }

    public static final int TRY_COUNT = 3;

    public static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors()>=8?Runtime.getRuntime().availableProcessors() * 2:16;

    public static final int QUEUE_SIZE = 100000;

    public static final long QUEUE_MEM_LIMIT_SIZE = 128 * 1024 * 1024;

    /**
     * Parameter type
     * Parameter type
     */
    public static final String PARAM_TYPE_BYTE = "byte";
    public static final String PARAM_TYPE_SHORT = "short";
    public static final String PARAM_TYPE_INT = "int";
    public static final String PARAM_TYPE_LONG = "long";
    public static final String PARAM_TYPE_FLOAT = "float";
    public static final String PARAM_TYPE_DOUBLE = "double";
}
