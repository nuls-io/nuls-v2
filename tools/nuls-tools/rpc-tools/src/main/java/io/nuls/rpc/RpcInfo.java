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

package io.nuls.rpc;

import io.nuls.rpc.pojo.Rpc;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tangyi
 * @date 2018/10/13
 * @description
 */
public class RpcInfo {

    /**
     * key format: cmd-version
     * value format: RpcInterface, include uri/handler
     */
    public static ConcurrentHashMap<String, Rpc> defaultInterfaceMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Rpc> localInterfaceMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Rpc> remoteInterfaceMap = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Long> heartbeatMap = new ConcurrentHashMap<>();

    /**
     * Default Value
     */
    public static final String CMD_JOIN = "join";
    public static final String CMD_LIST = "list";
    public static final String CMD_HEARTBEAT = "heartbeat";

    public static final String DEFAULT_PATH = "nulsrpc";

    public static final int VERSION = 1;

    public static final long HEARTBEATTIMEMILLIS = 60 * 1000;
    public static final String HEARTBEATREQUEST = "hello nuls?";
    public static final String HEARTBEATRESPONSE = "i'm everything!";


    public static void print() {
        System.out.println("接口详细信息如下：");
        System.out.println("系统默认的：" + defaultInterfaceMap.size());
        System.out.println("自己提供的：" + localInterfaceMap.size());
        System.out.println("远程获取的：" + remoteInterfaceMap.size());
    }
}
