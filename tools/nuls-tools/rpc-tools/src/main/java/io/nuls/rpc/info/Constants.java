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

import io.nuls.rpc.model.ErrorInfo;

/**
 * @author tangyi
 * @date 2018/10/19
 * @description
 */
public class Constants {

    public static final int KERNEL_PORT = 8091;
    public static final String DEFAULT_PATH = "nulsrpc";
    public static final String JSON = "jsonGo";
    public static final String BYTE = "byteGo";
//    public static final String MULTIPLY = "matchMultiply";

    public static final String FORM_PARAM_NAME = "paramObjAsJson";


    /**
     * WebSocket constant
     */
    public static final String ONLINE = "nuls_websocket_online:";
    public static final String OFFLINE = "nuls_websocket_offline:";
    public static final String DELIMIT = "-->>";


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
     * SUCCESS_CODE
     */
    public static final int SUCCESS_CODE = 0;

    /**
     * ErrorInfo
     */
    public static final ErrorInfo PARSE_ERROR = new ErrorInfo(-32700, "Parse error");
    public static final ErrorInfo INVALID_REQUEST = new ErrorInfo(-32600, "Invalid Request");
    public static final ErrorInfo METHOD_NOT_FOUND = new ErrorInfo(-32601, "Method not found");
    public static final ErrorInfo INVALID_PARAMS = new ErrorInfo(-32602, "Invalid params");
    public static final ErrorInfo INTERNAL_ERROR = new ErrorInfo(-32603, "Internal error");
}
