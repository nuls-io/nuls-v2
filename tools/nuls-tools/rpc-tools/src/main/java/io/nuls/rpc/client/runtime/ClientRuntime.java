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
package io.nuls.rpc.client.runtime;

import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.message.Message;
import io.nuls.tools.thread.TimeService;
import org.java_websocket.WebSocket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * 客户端运行时所需要的变量和方法
 * Variables and static methods required by client runtime
 *
 * @author tangyi
 * @date 2018/11/23
 */
public class ClientRuntime {

    /**
     * Key: 角色，Value：角色的连接信息
     * Key: role, Value: Connection information of the role
     */
    public static final Map<String, Map> ROLE_MAP = new ConcurrentHashMap<>();

    /**
     * 从服务端得到的握手确认
     * Handshake confirmation(NegotiateConnectionResponse) from the server
     */
    public static final Queue<Message> NEGOTIATE_RESPONSE_QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * 从服务端得到的请求确认
     * Request confirmation(Ack) from the server
     */
    public static final Queue<Message> ACK_QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * 从服务端得到的需要手动处理的应答消息
     * Response that need to be handled manually from the server
     */
    public static final Queue<Message> RESPONSE_MANUAL_QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * 从服务端得到的自动处理的应答消息
     * Response that need to be handled Automatically from the server
     */
    public static final Queue<Message> RESPONSE_AUTO_QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * 调用远程方法时，可以设置自动回调的本地方法。
     * Key：调用远程方法的messageId，Value：自动回调的本地方法
     * <p>
     * When calling a remote method, you can set the local method for automatic callback
     * Key: MessageId that calls remote methods, Value: Local method of automatic callback
     */
    public static final Map<String, BaseInvoke> INVOKE_MAP = new ConcurrentHashMap<>();

    /**
     * 连接其他模块的客户端集合
     * Key: 连接地址(如: ws://127.0.0.1:8887), Value：WsClient对象
     * <p>
     * Client Set Connecting Other Modules
     * Key: url(ex: ws://127.0.0.1:8887), Value: WsClient object
     */
    public static final Map<String, WsClient> WS_CLIENT_MAP = new ConcurrentHashMap<>();

    /**
     * messageId对应的客户端对象，用于取消订阅的Request
     * Key：messageId, Value：客户端对象
     * <p>
     * WsClient object corresponding to messageId, used to unsubscribe the Request
     * key: messageId, value: WsClient
     */
    public static final ConcurrentMap<String, WsClient> MSG_ID_KEY_WS_CLIENT_MAP = new ConcurrentHashMap<>();


    /**
     * 根据角色返回角色的连接信息
     * Return the role's connection information based on the role
     */
    public static String getRemoteUri(String role) {
        Map map = ROLE_MAP.get(role);
        return map == null
                ? null
                : "ws://" + map.get(Constants.KEY_IP) + ":" + map.get(Constants.KEY_PORT);
    }

    /**
     * @param url 连接字符串，Connection Url
     * @return 与url对应的客户端对象，WsClient object corresponding to URL
     * @throws Exception 连接失败，Connect failed
     */
    public static WsClient getWsClient(String url) throws Exception {
        if (!WS_CLIENT_MAP.containsKey(url)) {
            /*
            如果是第一次连接，则先放入集合
            If it's the first connection, put it in the collection first
             */
            WsClient wsClient = new WsClient(url);
            wsClient.connect();
            long start = TimeService.currentTimeMillis();
            while (!wsClient.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
                if (TimeService.currentTimeMillis() - start > Constants.MILLIS_PER_SECOND * 5) {
                    throw new Exception("Failed to connect " + url);
                }
                Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
            }
            WS_CLIENT_MAP.put(url, wsClient);
        }

        /*
        从Map中返回客户端对象
        Return WsClient objects from Map
         */
        return WS_CLIENT_MAP.get(url);
    }

    /**
     * 判断是否为正整数
     * Determine whether it is a positive integer
     *
     * @param string 待验证的值，Value to be verified
     * @return boolean
     */
    public static boolean isPureDigital(String string) {
        try {
            return Integer.parseInt(string) > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
