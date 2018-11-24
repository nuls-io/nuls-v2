package io.nuls.rpc.cmd;

import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.handler.CmdHandler;
import io.nuls.rpc.info.ClientRuntime;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.ServerRuntime;
import io.nuls.rpc.model.ModuleInfo;
import io.nuls.rpc.model.message.*;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.io.IOException;
import java.util.HashMap;
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
        int messageId = Constants.nextSequence();
        Message message = CmdHandler.basicMessage(messageId, MessageType.NegotiateConnection);
        message.setMessageData(CmdHandler.defaultNegotiateConnection());

        WsClient wsClient = ClientRuntime.getWsClient(Constants.kernelUrl);
        if (wsClient == null) {
            throw new Exception("Kernel not available");
        }
        Log.info("NegotiateConnection:" + JSONUtils.obj2json(message));
        wsClient.send(JSONUtils.obj2json(message));

        Message rspMessage = JSONUtils.json2pojo(callValue(messageId), Message.class);
        return MessageType.NegotiateConnectionResponse.name().equals(rspMessage.getMessageType());
    }

    /**
     * 1. send local module information to kernel
     * 2. receive all the modules' interfaces from kernel
     * 这个方法在Berzeck确定了他的最终JSON格式之后还要进行修改
     */
    public static void syncKernel() throws Exception {
        int messageId = Constants.nextSequence();
        Message message = CmdHandler.basicMessage(messageId, MessageType.Request);
        Request request = defaultRequest();
        request.getRequestMethods().put("registerAPI", ServerRuntime.local.getRegisterApi());
        message.setMessageData(request);

        WsClient wsClient = ClientRuntime.getWsClient(Constants.kernelUrl);
        if (wsClient == null) {
            throw new Exception("Kernel not available");
        }
        wsClient.send(JSONUtils.obj2json(message));

        @SuppressWarnings("unchecked")
        Map<String, Object> messageData = JSONUtils.json2map(callValue(messageId));
        Log.info("APIMethods from kernel:" + JSONUtils.obj2json(messageData));
        Map<String, Object> responseData = JSONUtils.json2map(JSONUtils.obj2json(messageData.get("responseData")));
        for (String key : responseData.keySet()) {
            ModuleInfo moduleInfo = JSONUtils.json2pojo(JSONUtils.obj2json(responseData.get(key)), ModuleInfo.class);
            ClientRuntime.remoteModuleMap.put(key, moduleInfo);
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
    public static Response requestAndResponse(String cmd, Map params) throws Exception {
        int messageId = request(cmd, params, 0);
        return callValueWithResponse(messageId);
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
    public static int request(String cmd, Map params, int subscriptionPeriod) throws Exception {
        int messageId = Constants.SEQUENCE.incrementAndGet();
        Message message = CmdHandler.basicMessage(messageId, MessageType.Request);
        Request request = defaultRequest();
        request.setSubscriptionPeriod(subscriptionPeriod);
        request.getRequestMethods().put(cmd, params);
        message.setMessageData(request);

        String uri = ClientRuntime.getRemoteUri(cmd);
        if (uri == null) {
            return -1;
        }
        WsClient wsClient = ClientRuntime.getWsClient(uri);
        Log.info("Request:" + JSONUtils.obj2json(message));
        wsClient.send(JSONUtils.obj2json(message));

        if (subscriptionPeriod > 0) {
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
    public static void unsubscribe(int messageId) throws Exception {
        Message message = CmdHandler.basicMessage(messageId, MessageType.Unsubscribe);
        Unsubscribe unsubscribe = new Unsubscribe();
        unsubscribe.setUnsubscribeMethods(new String[]{messageId + ""});
        message.setMessageData(unsubscribe);

        WsClient wsClient = ClientRuntime.msgIdKeyWsClientMap.get(messageId);
        if (wsClient != null) {
            wsClient.send(JSONUtils.obj2json(message));
        }
    }

    /**
     * Get response by messageId
     */
    public static String callValue(int messageId) throws InterruptedException, IOException {

        if (messageId < 0) {
            return JSONUtils.obj2json(ServerRuntime.newResponse(messageId, Constants.RESPONSE_STATUS_FAILED, Constants.CMD_NOT_FOUND));
        }

        long timeMillis = System.currentTimeMillis();
        do {

            for (Map map : ClientRuntime.CALLED_VALUE_QUEUE) {
                Message message = JSONUtils.map2pojo(map, Message.class);
                MessageType messageType = MessageType.valueOf(message.getMessageType());
                switch (messageType) {
                    case Response:
                        Response response = JSONUtils.map2pojo((Map) message.getMessageData(), Response.class);
                        if (response.getRequestId() == messageId) {
                            synchronized (ClientRuntime.CALLED_VALUE_QUEUE) {
                                ClientRuntime.CALLED_VALUE_QUEUE.remove(map);
                            }
                            Log.info("Response:" + JSONUtils.obj2json(response));
                            return JSONUtils.obj2json(response);
                        }
                        break;
                    case NegotiateConnectionResponse:
                        synchronized (ClientRuntime.CALLED_VALUE_QUEUE) {
                            ClientRuntime.CALLED_VALUE_QUEUE.remove(map);
                        }
                        Log.info("NegotiateConnectionResponse:" + JSONUtils.obj2json(map));
                        return JSONUtils.obj2json(map);
                    case Ack:
                        synchronized (ClientRuntime.CALLED_VALUE_QUEUE) {
                            ClientRuntime.CALLED_VALUE_QUEUE.remove(map);
                        }
                        Log.info("Ack:" + JSONUtils.obj2json(map));
                        return JSONUtils.obj2json(map);
                    default:
                        break;
                }
            }

            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        } while (System.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS);

        return JSONUtils.obj2json(ServerRuntime.newResponse(messageId, Constants.RESPONSE_STATUS_FAILED, Constants.RESPONSE_TIMEOUT));
    }

    /**
     * Get response by messageId
     */
    public static Response callValueWithResponse(int messageId) throws InterruptedException, IOException {

        if (messageId < 0) {
            return ServerRuntime.newResponse(messageId, Constants.RESPONSE_STATUS_FAILED, Constants.CMD_NOT_FOUND);
        }

        long timeMillis = System.currentTimeMillis();
        do {

            for (Map map : ClientRuntime.CALLED_VALUE_QUEUE) {
                Message message = JSONUtils.map2pojo(map, Message.class);
                MessageType messageType = MessageType.valueOf(message.getMessageType());
                switch (messageType) {
                    case Response:
                        Response response = JSONUtils.map2pojo((Map) message.getMessageData(), Response.class);
                        if (response.getRequestId() == messageId) {
                            synchronized (ClientRuntime.CALLED_VALUE_QUEUE) {
                                ClientRuntime.CALLED_VALUE_QUEUE.remove(map);
                            }
                            Log.info("Response:" + JSONUtils.obj2json(response));
                            return response;
                        }
                        break;
                    default:
                        break;
                }
            }

            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        } while (System.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS);

        return ServerRuntime.newResponse(messageId, Constants.RESPONSE_STATUS_FAILED, Constants.RESPONSE_TIMEOUT);
    }


    /**
     * Constructing a default Request object
     */
    private static Request defaultRequest() {
        Request request = new Request();
        request.setRequestAck(0);
        request.setSubscriptionEventCounter(0);
        request.setSubscriptionPeriod(0);
        request.setSubscriptionRange("");
        request.setResponseMaxSize(0);
        request.setRequestMethods(new HashMap<>(16));
        return request;
    }
}
