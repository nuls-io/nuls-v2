package io.nuls.rpc.client;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.*;
import io.nuls.rpc.server.CmdHandler;
import io.nuls.rpc.server.ServerRuntime;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.util.Map;

/**
 * Command dispatcher
 * All commands should be invoked through this class
 *
 * @author tangyi
 * @date 2018/11/5
 * @description
 */
public class CmdDispatcher {

    /**
     * Handshake with kernel
     */
    public static boolean handshakeKernel() throws Exception {
        String messageId = Constants.nextSequence();
        Message message = CmdHandler.basicMessage(messageId, MessageType.NegotiateConnection);
        message.setMessageData(CmdHandler.defaultNegotiateConnection());

        WsClient wsClient = ClientRuntime.getWsClient(Constants.kernelUrl);
        if (wsClient == null) {
            throw new Exception("Kernel not available");
        }
        Log.info("NegotiateConnection:" + JSONUtils.obj2json(message));
        wsClient.send(JSONUtils.obj2json(message));

        Message rspMessage = callMessage(MessageType.NegotiateConnectionResponse);
        return rspMessage != null;
    }

    /**
     * 1. send local module information to kernel
     * 2. receive all the modules' interfaces from kernel
     */
    public static void syncKernel() throws Exception {
        String messageId = Constants.nextSequence();
        Message message = CmdHandler.basicMessage(messageId, MessageType.Request);
        Request request = ClientRuntime.defaultRequest();
        request.getRequestMethods().put("registerAPI", ServerRuntime.local);
        message.setMessageData(request);

        WsClient wsClient = ClientRuntime.getWsClient(Constants.kernelUrl);
        if (wsClient == null) {
            throw new Exception("Kernel not available");
        }
        wsClient.send(JSONUtils.obj2json(message));

        Response response = getResponseByMessageId(messageId);
        Log.info("APIMethods from kernel:" + JSONUtils.obj2json(response));
        Map responseData = (Map) response.getResponseData();
        Map methodMap = (Map) responseData.get("registerAPI");
        Map dependMap = (Map) methodMap.get("Dependencies");
        for (Object key : dependMap.keySet()) {
            ClientRuntime.roleMap.put(key.toString(), (Map) dependMap.get(key));
        }
    }


    /**
     * call cmd.
     * 1. Find the corresponding module according to cmd
     * 2. Send to the specified module
     * 3. Get the result returned to the caller
     * 4. Get the highest version of cmd
     *
     * @return Result with JSON string
     */
    public static Response requestAndResponse(String role, String cmd, Map params) throws Exception {
        String messageId = request(role, cmd, params, Constants.booleanString(false), "0");
        return getResponseByMessageId(messageId);
    }

    public static String requestAndInvoke(String role, String cmd, Map params, String subscriptionPeriod, Class clazz, String invokeMethod) throws Exception {
        if (Integer.parseInt(subscriptionPeriod) <= 0) {
            throw new Exception("subscriptionPeriod must great than 0");
        }
        String messageId = request(role, cmd, params, Constants.booleanString(false), subscriptionPeriod);
        ClientRuntime.INVOKE_MAP.put(messageId, new Object[]{clazz, invokeMethod});
        return messageId;
    }

    public static boolean requestAndAck(String role, String cmd, Map params, String subscriptionPeriod, Class clazz, String invokeMethod) throws Exception {
        String messageId = request(role, cmd, params, Constants.booleanString(true), subscriptionPeriod);
        ClientRuntime.INVOKE_MAP.put(messageId, new Object[]{clazz, invokeMethod});
        return getAckByMessageId(messageId);
    }

    /**
     * call cmd.
     * 1. Find the corresponding module according to cmd
     * 2. Send to the specified module
     * 3. Get the result returned to the caller
     * 4. Get the highest version of cmd
     *
     * @return Message ID
     */
    private static String request(String role, String cmd, Map params, String ack, String subscriptionPeriod) throws Exception {
        String messageId = Constants.nextSequence();
        Message message = CmdHandler.basicMessage(messageId, MessageType.Request);
        Request request = ClientRuntime.defaultRequest();
        request.setRequestAck(ack);
        request.setSubscriptionPeriod(subscriptionPeriod);
        request.getRequestMethods().put(cmd, params);
        message.setMessageData(request);

        String uri = ClientRuntime.getRemoteUri(role);
        if (uri == null) {
            return "-1";
        }
        WsClient wsClient = ClientRuntime.getWsClient(uri);
        Log.info("SendRequest to " + wsClient.getRemoteSocketAddress().getHostString() + ":" + wsClient.getRemoteSocketAddress().getPort() + "->" + JSONUtils.obj2json(message));
        wsClient.send(JSONUtils.obj2json(message));

        if (Integer.parseInt(subscriptionPeriod) > 0) {
            ClientRuntime.msgIdKeyWsClientMap.put(messageId, wsClient);
        }

        return messageId;
    }


    /**
     * Method of Unsubscribe
     * A call that responds only once does not need to be cancelled
     *
     * @param messageId Request message ID
     */
    public static void unsubscribe(String messageId) throws Exception {
        Message message = CmdHandler.basicMessage(Constants.nextSequence(), MessageType.Unsubscribe);
        Unsubscribe unsubscribe = new Unsubscribe();
        unsubscribe.setUnsubscribeMethods(new String[]{messageId});
        message.setMessageData(unsubscribe);

        WsClient wsClient = ClientRuntime.msgIdKeyWsClientMap.get(messageId);
        if (wsClient != null) {
            wsClient.send(JSONUtils.obj2json(message));
            ClientRuntime.INVOKE_MAP.remove(messageId);
        }
    }

    /**
     * Get response by messageId
     */
    private static Message callMessage(MessageType messageType) throws InterruptedException, IOException {

        long timeMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS) {
            synchronized (ClientRuntime.CALLED_VALUE_QUEUE) {
                for (Map map : ClientRuntime.CALLED_VALUE_QUEUE) {
                    Message message = JSONUtils.map2pojo(map, Message.class);
                    if (messageType.name().equals(message.getMessageType())) {
                        ClientRuntime.CALLED_VALUE_QUEUE.remove(map);
                        Log.info(message.getMessageType() + ":" + JSONUtils.obj2json(message));
                        return message;
                    }
                }
            }
            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        }

        return null;
    }

    /**
     * Get response by messageId
     */
    private static Response getResponseByMessageId(String messageId) throws InterruptedException, IOException {
        if (Integer.parseInt(messageId) < 0) {
            return ServerRuntime.newResponse(messageId, Constants.booleanString(false), Constants.CMD_NOT_FOUND);
        }

        long timeMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS) {
            synchronized (ClientRuntime.CALLED_VALUE_QUEUE) {
                for (Map map : ClientRuntime.CALLED_VALUE_QUEUE) {
                    Message message = JSONUtils.map2pojo(map, Message.class);
                    if (!MessageType.Response.name().equals(message.getMessageType())) {
                        continue;
                    }

                    Response response = JSONUtils.map2pojo((Map) message.getMessageData(), Response.class);
                    if (response.getRequestId().equals(messageId)) {
                        ClientRuntime.CALLED_VALUE_QUEUE.remove(map);
                        Log.info("Response:" + JSONUtils.obj2json(response));
                        return response;
                    }

                }
            }
            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        }

        return ServerRuntime.newResponse(messageId, Constants.booleanString(false), Constants.RESPONSE_TIMEOUT);
    }

    /**
     * Get ack by messageId
     */
    private static boolean getAckByMessageId(String messageId) throws InterruptedException, IOException {
        if (Integer.parseInt(messageId) < 0) {
            return false;
        }

        long timeMillis = TimeService.currentTimeMillis();
        while (TimeService.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS) {
            synchronized (ClientRuntime.CALLED_VALUE_QUEUE) {
                for (Map map : ClientRuntime.CALLED_VALUE_QUEUE) {
                    Message message = JSONUtils.map2pojo(map, Message.class);
                    if (!MessageType.Ack.name().equals(message.getMessageType())) {
                        continue;
                    }

                    Ack ack = JSONUtils.map2pojo((Map) message.getMessageData(), Ack.class);
                    if (ack.getRequestId().equals(messageId)) {
                        ClientRuntime.CALLED_VALUE_QUEUE.remove(map);
                        Log.info("Ack:" + JSONUtils.obj2json(ack));
                        return true;
                    }
                }
            }
            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        }

        return false;
    }

}
