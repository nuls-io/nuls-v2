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
import org.java_websocket.WebSocket;

import java.util.*;

/**
 * @author tangyi
 * @date 2018/10/30
 * @description
 */
public class WsPool {
    /**
     * key: client WebSocket
     * value: the uri of server which used to send response message
     */
    private static final Map<WebSocket, WsClient> WS_MAP = new HashMap<>();



    /**
     * 通过websocket连接获取其对应的用户
     */
    public static WsClient getClientByWs(WebSocket webSocket) {
        return WS_MAP.get(webSocket);
    }


    /**
     * 向连接池中添加连接
     */
    public static void addClient(WebSocket webSocket, WsClient wsClient) {
        WS_MAP.put(webSocket, wsClient);
    }

    /**
     * 获取所有连接池中的用户，因为set是不允许重复的，所以可以得到无重复的user数组
     */
    public static Collection<WsClient> getOnlineUri() {
        return new ArrayList<>(WS_MAP.values());
    }

    /**
     * 移除连接池中的连接
     */
    public static void removeClient(WebSocket webSocket) {
        WS_MAP.remove(webSocket);
    }

    /**
     * 向特定的用户发送数据
     */
    public static void sendMessageToClient(WebSocket webSocket, String message) {
        if (null != webSocket && null != WS_MAP.get(webSocket)) {
            webSocket.send(message);
        }
    }

    /**
     * 向所有的用户发送消息
     */
    public static void sendMessageToAll(String message) {
        Set<WebSocket> keySet = WS_MAP.keySet();
        synchronized (WS_MAP) {
            for (WebSocket webSocket : keySet) {
                WsClient wsClient = WS_MAP.get(webSocket);
                if (wsClient != null) {
                    webSocket.send(message);
                }
            }
        }
    }

}
