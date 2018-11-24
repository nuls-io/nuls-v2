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

package io.nuls.account.constant;


import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Na;

/**
 * @author: qinyifeng
 * @description: RPC常量 RPC constants
 */
public interface RpcConstant {

    /**
     * --------[call other module RPC constants] -------
     */
    /**
     * EVENT_SEND_CMD
     */
    String EVENT_SEND_CMD = "send";
    /**
     * EVENT_SEND_VERSION
     */
    String EVENT_SEND_VERSION = "1.0";
    /**
     * EVENT_SEND_TOPIC
     */
    String EVENT_SEND_TOPIC = "topic";
    /**
     * EVENT_SEND_DATA
     */
    String EVENT_SEND_DATA = "data";

    /**
     * --------[RPC response constants] -------
     */
    /**
     * value
     */
    String VALUE = "value";
    /**
     * address
     */
    String ADDRESS = "address";
    /**
     * encryptedPriKey
     */
    String ENCRYPTED_PRIKEY = "encryptedPriKey";
    /**
     * path
     */
    String PATH = "path";

}
