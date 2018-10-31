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

import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.model.ConfigItem;
import io.nuls.rpc.model.Module;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author tangyi
 * @date 2018/10/13
 * @description
 */
public class RuntimeParam {

    /**
     * local module(io.nuls.rpc.Module) information
     */
    public static Module local;

    /**
     * local Config item information
     */
    public static List<ConfigItem> configItemList = Collections.synchronizedList(new ArrayList<>());

    /**
     * remote module information
     * key: module name/code
     * value: module(io.nuls.rpc.Module)
     */
    public static ConcurrentMap<String, Module> remoteModuleMap = new ConcurrentHashMap<>();

    /**
     * cmd sequence
     */
    public static AtomicInteger sequence = new AtomicInteger(0);

    /**
     * result queue
     */
    public static List<Map> resultQueue = Collections.synchronizedList(new ArrayList<>());

    /**
     * WebSocket clients
     * The client and the module correspond one by one
     * key: uri(ws://127.0.0.1:8887)
     * value: WsClient
     */
    private static ConcurrentMap<String, WsClient> wsClientMap = new ConcurrentHashMap<>();

    /**
     * get WsClient through uri
     */
    public static WsClient getWsClient(String uri) throws Exception {
        if (!wsClientMap.containsKey(uri)) {
            WsClient wsClient = new WsClient(uri);
            wsClient.connect();
            while (!wsClient.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
                Thread.sleep(10);
            }
            wsClientMap.put(uri, wsClient);
        }
        return wsClientMap.get(uri);
    }

    /**
     * get the next call counter
     */
    public static int nextSequence() {
        return sequence.incrementAndGet();
    }
}
