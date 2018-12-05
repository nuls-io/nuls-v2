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
package io.nuls.rpc.client;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.message.*;
import io.nuls.rpc.server.ServerRuntime;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.util.Map;

/**
 * 用于调用远程方法的类，提供多种调用方式，也只应该使用这个类来调用
 * Classes used to call remote methods, provide multiple ways of invoking, and only this class should be used to invoke
 *
 * @author tangyi
 * @date 2018/11/5
 */
public class CmdDispatcher {

    /**
     * 与核心模块（Manager）握手
     * Shake hands with the core module (Manager)
     *
     * @return boolean
     * @throws Exception 握手失败, handshake failed
     */
    public static boolean handshakeManager() throws Exception {
        Message message = MessageUtil.basicMessage(MessageType.NegotiateConnection);
        message.setMessageData(MessageUtil.defaultNegotiateConnection());

        WsClient wsClient = ClientRuntime.getWsClient(Constants.kernelUrl);

        if (wsClient == null) {
            throw new Exception("Kernel not available");
        }
        Log.info("NegotiateConnection:" + JSONUtils.obj2json(message));
        wsClient.send(JSONUtils.obj2json(message));

        /*
        是否收到正确的握手确认
        Whether received the correct handshake confirmation?
         */
        return receiveNegotiateConnectionResponse();
    }

    /**
     * 同步本地模块与核心模块（Manager）
     * 1. 发送本地信息给Manager
     * 2. 获取本地所依赖的角色的连接信息
     * Synchronize Local Module and Core Module (Manager)
     * 1. Send local information to Manager
     * 2. Get connection information for locally dependent roles
     *
     * @throws Exception 核心模块（Manager）不可用，Core Module (Manager) Not Available
     */
    public static void syncManager() throws Exception {

        Message message = MessageUtil.basicMessage(MessageType.Request);
        Request request = MessageUtil.defaultRequest();
        request.getRequestMethods().put("registerAPI", ServerRuntime.local);
        message.setMessageData(request);

        WsClient wsClient = ClientRuntime.getWsClient(Constants.kernelUrl);
        if (wsClient == null) {
            throw new Exception("Kernel not available");
        }
        wsClient.send(JSONUtils.obj2json(message));

        Response response = receiveResponse(message.getMessageId());
        Map responseData = (Map) response.getResponseData();
        Map methodMap = (Map) responseData.get("registerAPI");
        Map dependMap = (Map) methodMap.get("Dependencies");
        for (Object key : dependMap.keySet()) {
            ClientRuntime.roleMap.put(key.toString(), (Map) dependMap.get(key));
        }
        Log.info("Sync manager success. " + JSONUtils.obj2json(ClientRuntime.roleMap));
    }


    /**
     * 发送Request，并等待Response，如果等待超过1分钟，则抛出超时异常
     * Send Request and wait for Response, and throw a timeout exception if the waiting time more than one minute
     */
    public static Response requestAndResponse(String role, String cmd, Map params) throws Exception {
        Request request = MessageUtil.newRequest(cmd, params, Constants.BOOLEAN_FALSE, Constants.ZERO, Constants.ZERO);
        String messageId = sendRequest(role, request);
        return receiveResponse(messageId);
    }

    /**
     * 发送Request，并根据返回结果自动调用本地方法
     * 返回值为messageId，用以取消订阅
     * Send the Request and automatically call the local method based on the return result
     * The return value is messageId, used to unsubscribe
     */
    public static String requestAndInvoke(String role, String cmd, Map params, String subscriptionPeriod, String subscriptionEventCounter, BaseInvoke baseInvoke) throws Exception {
        Request request = MessageUtil.newRequest(cmd, params, Constants.BOOLEAN_FALSE, subscriptionPeriod, subscriptionEventCounter);
        String messageId = sendRequest(role, request);
        ClientRuntime.INVOKE_MAP.put(messageId, baseInvoke);
        return messageId;
    }

    /**
     * 与requestAndInvoke类似，但是发送之后必须接收到一个Ack作为确认
     * Similar to requestAndInvoke, but after sending, an Ack must be received as an acknowledgement
     */
    public static String requestAndInvokeWithAck(String role, String cmd, Map params, String subscriptionPeriod, String subscriptionEventCounter, BaseInvoke baseInvoke) throws Exception {
        Request request = MessageUtil.newRequest(cmd, params, Constants.BOOLEAN_TRUE, subscriptionPeriod, subscriptionEventCounter);
        String messageId = sendRequest(role, request);
        ClientRuntime.INVOKE_MAP.put(messageId, baseInvoke);
        return receiveAck(messageId) ? messageId : null;
    }

    /**
     * 发送Request，自己封装Request对象(可以一次调用多个cmd)
     * Send Request, need to wrap the Request object manually(for calling multiple methods at a time)
     */
    public static String requestAndInvoke(String role, Request request, BaseInvoke baseInvoke) throws Exception {
        if (ClientRuntime.isPureDigital(request.getSubscriptionPeriod())
                || ClientRuntime.isPureDigital(request.getSubscriptionEventCounter())) {
            throw new Exception("Wrong value: [SubscriptionPeriod][SubscriptionEventCounter]");
        }

        String messageId = sendRequest(role, request);
        ClientRuntime.INVOKE_MAP.put(messageId, baseInvoke);
        if (Constants.BOOLEAN_FALSE.equals(request.getRequestAck())) {
            return messageId;
        } else {
            return receiveAck(messageId) ? messageId : null;
        }
    }


    /**
     * 发送Request，返回该Request的messageId
     * Send Request, return the messageId of the Request
     */
    private static String sendRequest(String role, Request request) throws Exception {

        Message message = MessageUtil.basicMessage(MessageType.Request);
        message.setMessageData(request);

        /*
        从roleMap获取命令需发送到的地址
        Get the url from roleMap which the command needs to be sent to
         */
        String url = ClientRuntime.getRemoteUri(role);
        if (url == null) {
            return "-1";
        }
        WsClient wsClient = ClientRuntime.getWsClient(url);
        Log.info("SendRequest to " + wsClient.getRemoteSocketAddress().getHostString() + ":" + wsClient.getRemoteSocketAddress().getPort() + "->" + JSONUtils.obj2json(message));
        wsClient.send(JSONUtils.obj2json(message));

        if (ClientRuntime.isPureDigital(request.getSubscriptionPeriod())
                || ClientRuntime.isPureDigital(request.getSubscriptionEventCounter())) {
            /*
            如果是需要重复发送的消息（订阅消息），记录messageId与客户端的对应关系，用于取消订阅
            If it is a message (subscription message) that needs to be sent repeatedly, record the relationship between the messageId and the WsClient
             */
            ClientRuntime.msgIdKeyWsClientMap.put(message.getMessageId(), wsClient);
        }

        return message.getMessageId();
    }


    /**
     * 取消订阅
     * Unsubscribe
     */
    public static void sendUnsubscribe(String messageId) throws Exception {
        Message message = MessageUtil.basicMessage(MessageType.Unsubscribe);
        Unsubscribe unsubscribe = new Unsubscribe();
        unsubscribe.setUnsubscribeMethods(new String[]{messageId});
        message.setMessageData(unsubscribe);

        /*
        根据messageId获取WsClient，发送取消订阅命令，然后移除本地信息
        Get the WsClient according to messageId, send the unsubscribe command, and then remove the local information
         */
        WsClient wsClient = ClientRuntime.msgIdKeyWsClientMap.get(messageId);
        if (wsClient != null) {
            wsClient.send(JSONUtils.obj2json(message));
            Log.info("取消订阅：" + JSONUtils.obj2json(message));
            ClientRuntime.INVOKE_MAP.remove(messageId);
        }
    }

    /**
     * 是否握手成功
     * Whether shake hands successfully?
     */
    static boolean receiveNegotiateConnectionResponse() throws InterruptedException {

        long timeMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS) {
            /*
            获取队列中的第一个对象，如果非空，则说明握手成功
            Get the first item of the queue, If not empty, the handshake is successful.
             */
            Message message = ClientRuntime.firstMessageInNegotiateResponseQueue();
            if (message != null) {
                return true;
            }

            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        }

        /*
        Timeout Error
         */
        return false;
    }

    /**
     * 根据messageId获取Response
     * Get response by messageId
     */
    private static Response receiveResponse(String messageId) throws InterruptedException, IOException {

        long timeMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS) {
            /*
            获取队列中的第一个对象，如果是空，舍弃
            Get the first item of the queue, If it is an empty object, discard
             */
            Message message = ClientRuntime.firstMessageInResponseManualQueue();
            if (message == null) {
                Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
                continue;
            }

            Response response = JSONUtils.map2pojo((Map) message.getMessageData(), Response.class);
            if (response.getRequestId().equals(messageId)) {
                /*
                messageId匹配，说明就是需要的结果，返回
                If messageId is the same, then the response is needed
                 */
                Log.info("Response:" + JSONUtils.obj2json(message));
                return response;
            }

            /*
            messageId不匹配，放回队列
            Add back to the queue
             */
            ClientRuntime.RESPONSE_MANUAL_QUEUE.offer(message);

            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        }

        /*
        Timeout Error
         */
        return MessageUtil.newResponse(messageId, Constants.BOOLEAN_FALSE, Constants.RESPONSE_TIMEOUT);
    }

    /**
     * 获取收到Request的确认
     * Get confirmation of receipt(Ack) of Request
     */
    private static boolean receiveAck(String messageId) throws InterruptedException, IOException {

        long timeMillis = TimeService.currentTimeMillis();
        while (TimeService.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS) {
            /*
            获取队列中的第一个对象，如果是空，舍弃
            Get the first item of the queue, If it is an empty object, discard
             */
            Message message = ClientRuntime.firstMessageInAckQueue();
            if (message == null) {
                Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
                continue;
            }

            Ack ack = JSONUtils.map2pojo((Map) message.getMessageData(), Ack.class);
            if (ack.getRequestId().equals(messageId)) {
                /*
                messageId匹配，说明就是需要的结果，返回
                If messageId is the same, then the ack is needed
                 */
                Log.info("Ack:" + JSONUtils.obj2json(ack));
                return true;
            }

            /*
            messageId不匹配，放回队列
            Add back to the queue
             */
            ClientRuntime.ACK_QUEUE.offer(message);

            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        }

        /*
        Timeout Error
         */
        return false;
    }

}
