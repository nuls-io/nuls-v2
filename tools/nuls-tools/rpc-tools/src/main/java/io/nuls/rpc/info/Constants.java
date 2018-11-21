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

/**
 * @author tangyi
 * @date 2018/10/19
 * @description
 */
public class Constants {

    private Constants() {
    }

    /**
     * WebSocket constant
     */
    public static final long INTERVAL_TIMEMILLIS = 100;
    public static final long TIMEOUT_TIMEMILLIS = 60 * 1000;
    public static final String VERSION_KEY_STR = "version";
    public static final String PUBLIC = "public";
    public static final String PRIVATE = "private";
    public static final String ADMIN = "admin";

    /**
     * Message type
     */
    public static final String NEGOTIATE_CONNECTION = "NegotiateConnection";
    public static final String NEGOTIATE_CONNECTION_RESPONSE = "NegotiateConnectionResponse";
    public static final String REQUEST = "Request";
    public static final String UNSUBSCRIBE = "Unsubscribe";
    public static final String RESPONSE = "Response";
    public static final String ACK = "Ack";
    public static final String REGISTER_COMPOUND_METHOD = "RegisterCompoundMethod";
    public static final String UNREGISTER_COMPOUND_METHOD = "UnregisterCompoundMethod";

    /**
     * Request type
     */
    public static final int REQUEST_TYPE_1 = 1;
    public static final int REQUEST_TYPE_2 = 2;
    public static final int REQUEST_TYPE_3 = 3;
    public static final int REQUEST_TYPE_4 = 4;

    /**
     * RPC error message
     */
    public static final String RESPONSE_TIMEOUT = "Response timeout";
    public static final String CMD_NOT_FOUND = "Cmd not found";
    public static final String CMD_DUPLICATE = "Duplicate cmd found";
    public static final String SERVICE_NOT_AVAILABLE = "Service not available";

    /**
     * predetermined cmd (used by kernel & module)
     */
    public static final String STATUS = "status";
    public static final String SHUTDOWN = "shutdown";
    public static final String TERMINATE = "terminate";
    public static final String CONF_GET = "conf_get";
    public static final String CONF_SET = "conf_set";
    public static final String CONF_RESET = "conf_reset";

    /**
     * RESPONSE_CODE
     */
    public static final String SUCCESS_CODE = "0";
    public static final String FAILED_CODE = "-1";

}
