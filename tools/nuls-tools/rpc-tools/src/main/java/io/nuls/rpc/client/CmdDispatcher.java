package io.nuls.rpc.client;

import io.nuls.rpc.info.Constants;
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
 * @description
 */
public class CmdDispatcher {

    /**
     * 与核心模块（Manager）握手
     * Shake hands with the core module (Manager)
     *
     * @return boolean
     * @throws Exception
     */
    public static boolean handshakeKernel() throws Exception {
        Message message = Constants.basicMessage(Constants.nextSequence(), MessageType.NegotiateConnection);
        message.setMessageData(Constants.defaultNegotiateConnection());

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
        return getNegotiateConnectionResponse();
    }

    /**
     * 同步本地模块与核心模块（Manager）
     * 1. 发送本地信息给Manager
     * 2. 获取本地所依赖的角色的连接信息
     * Synchronize Local Module and Core Module (Manager)
     * 1. Send local information to Manager
     * 2. Get connection information for locally dependent roles
     * role - connection
     */
    public static void syncKernel() throws Exception {
        String messageId = Constants.nextSequence();
        Message message = Constants.basicMessage(messageId, MessageType.Request);
        Request request = ClientRuntime.defaultRequest();
        request.getRequestMethods().put("registerAPI", ServerRuntime.local);
        message.setMessageData(request);

        WsClient wsClient = ClientRuntime.getWsClient(Constants.kernelUrl);
        if (wsClient == null) {
            throw new Exception("Kernel not available");
        }
        wsClient.send(JSONUtils.obj2json(message));

        Response response = getResponse(messageId);
        Log.info("APIMethods from kernel:" + JSONUtils.obj2json(response));
        Map responseData = (Map) response.getResponseData();
        Map methodMap = (Map) responseData.get("registerAPI");
        Map dependMap = (Map) methodMap.get("Dependencies");
        for (Object key : dependMap.keySet()) {
            ClientRuntime.roleMap.put(key.toString(), (Map) dependMap.get(key));
        }
    }


    /**
     * 发送Request，并等待Response，如果等待超过1分钟，则抛出超时异常
     * Send Request and wait for Response, and throw a timeout exception if the waiting time more than one minute
     */
    public static Response requestAndResponse(String role, String cmd, Map params) throws Exception {
        String messageId = request(role, cmd, params, Constants.booleanString(false), "0");
        return getResponse(messageId);
    }

    /**
     * 发送Request，并根据返回结果自动调用本地方法
     * 返回值为messageId，用以取消订阅
     * Send the Request and automatically call the local method based on the return result
     * The return value is messageId, used to unsubscribe
     */
    public static String requestAndInvoke(String role, String cmd, Map params, String subscriptionPeriod, Class clazz, String invokeMethod) throws Exception {
        String messageId = request(role, cmd, params, Constants.booleanString(false), subscriptionPeriod);
        ClientRuntime.INVOKE_MAP.put(messageId, new Object[]{clazz, invokeMethod});
        return messageId;
    }

    /**
     * 与requestAndInvoke类似，但是发送之后必须接收到一个Ack作为确认
     * Similar to requestAndInvoke, but after sending, an Ack must be received as an acknowledgement
     */
    public static String requestAndInvokeWithAck(String role, String cmd, Map params, String subscriptionPeriod, Class clazz, String invokeMethod) throws Exception {
        String messageId = request(role, cmd, params, Constants.booleanString(true), subscriptionPeriod);
        ClientRuntime.INVOKE_MAP.put(messageId, new Object[]{clazz, invokeMethod});
        return getAck(messageId) ? messageId : null;
    }

    /**
     * 发送Request，用于一次调用多个方法（需要自己封装Request对象）
     * Send Request for calling multiple methods at a time (need to wrap the Request object manually)
     */
    public static String requestAndInvoke(String role, Request request, Class clazz, String invokeMethod) throws Exception {
        String messageId = request(role, request);
        ClientRuntime.INVOKE_MAP.put(messageId, new Object[]{clazz, invokeMethod});
        if (Constants.booleanString(false).equals(request.getRequestAck())) {
            return messageId;
        } else {
            return getAck(messageId) ? messageId : null;
        }
    }

    /**
     * 根据参数构造Request对象，然后发送Request
     * Construct the Request object according to the parameters, and then send the Request
     */
    private static String request(String role, String cmd, Map params, String ack, String subscriptionPeriod) throws Exception {
        Request request = ClientRuntime.defaultRequest();
        request.setRequestAck(ack);
        request.setSubscriptionPeriod(subscriptionPeriod);
        request.getRequestMethods().put(cmd, params);
        return request(role, request);
    }

    /**
     * 发送Request，返回该Request的messageId
     * Send Request, return the messageId of the Request
     */
    private static String request(String role, Request request) throws Exception {
        String messageId = Constants.nextSequence();
        Message message = Constants.basicMessage(messageId, MessageType.Request);
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

        if (Integer.parseInt(request.getSubscriptionPeriod()) > 0) {
            /*
            如果是需要重复发送的消息（订阅消息），记录messageId与客户端的对应关系，用于取消订阅
            If it is a message (subscription message) that needs to be sent repeatedly, record the relationship between the messageId and the WsClient
             */
            ClientRuntime.msgIdKeyWsClientMap.put(messageId, wsClient);
        }

        return messageId;
    }


    /**
     * 取消订阅
     * Unsubscribe
     */
    public static void unsubscribe(String messageId) throws Exception {
        Message message = Constants.basicMessage(Constants.nextSequence(), MessageType.Unsubscribe);
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
            ClientRuntime.INVOKE_MAP.remove(messageId);
        }
    }

    /**
     * 是否握手成功
     * Whether shake hands successfully?
     */
    private static boolean getNegotiateConnectionResponse() throws InterruptedException, IOException {

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
    private static Response getResponse(String messageId) throws InterruptedException, IOException {

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
        return ServerRuntime.newResponse(messageId, Constants.booleanString(false), Constants.RESPONSE_TIMEOUT);
    }

    /**
     * 获取收到Request的确认
     * Get confirmation of receipt(Ack) of Request
     */
    private static boolean getAck(String messageId) throws InterruptedException, IOException {

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
