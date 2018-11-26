package io.nuls.rpc.client;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.*;
import io.nuls.rpc.server.CmdHandler;
import io.nuls.rpc.server.ServerRuntime;
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
     * 这个方法在Berzeck确定了他的最终JSON格式之后还要进行修改
     */
    public static void syncKernel() throws Exception {
        String messageId = Constants.nextSequence();
        Message message = CmdHandler.basicMessage(messageId, MessageType.Request);
        Request request = defaultRequest();
        request.getRequestMethods().put("registerAPI", ServerRuntime.local);
        message.setMessageData(request);

        WsClient wsClient = ClientRuntime.getWsClient(Constants.kernelUrl);
        if (wsClient == null) {
            throw new Exception("Kernel not available");
        }
        wsClient.send(JSONUtils.obj2json(message));

        @SuppressWarnings("unchecked")
        Response response = callMessageResponse(messageId);
        Log.info("APIMethods from kernel:" + JSONUtils.obj2json(response));
        Map<String, Object> responseData = (Map) response.getResponseData();
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
        String messageId = request(role, cmd, params, "0");
        return callMessageResponse(messageId);
    }

    public static void requestAndInvoke(String role, String cmd, Map params, String subscriptionPeriod, Class clazz, String method) throws Exception {
        String messageId = request(role, cmd, params, "0");
        Response response=callMessageResponse(messageId);
        /*
        Call through reflection
         */

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
    public static String request(String role, String cmd, Map params, String subscriptionPeriod) throws Exception {
        String messageId = Constants.nextSequence();
        Message message = CmdHandler.basicMessage(messageId, MessageType.Request);
        Request request = defaultRequest();
        request.setSubscriptionPeriod(subscriptionPeriod);
        request.getRequestMethods().put(cmd, params);
        message.setMessageData(request);

        String uri = ClientRuntime.getRemoteUri(role);
        if (uri == null) {
            return "-1";
        }
        WsClient wsClient = ClientRuntime.getWsClient(uri);
        Log.info("Request:" + JSONUtils.obj2json(message));
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
        }
    }

    /**
     * Get response by messageId
     */
    public static Message callMessage(MessageType messageType) throws InterruptedException, IOException {

        long timeMillis = System.currentTimeMillis();
        do {
            synchronized (ClientRuntime.CALLED_VALUE_QUEUE) {
                for (Map map : ClientRuntime.CALLED_VALUE_QUEUE) {
                    Message message = JSONUtils.map2pojo(map, Message.class);
                    if (messageType.name().equals(message.getMessageType())) {
                        ClientRuntime.CALLED_VALUE_QUEUE.remove(map);
                        Log.info("NegotiateConnectionResponse:" + JSONUtils.obj2json(message));
                        return message;
                    }

//                    switch (messageType) {
//                        case NegotiateConnectionResponse:
//                            ClientRuntime.CALLED_VALUE_QUEUE.remove(map);
//                            Log.info("NegotiateConnectionResponse:" + JSONUtils.obj2json(message));
//                            return JSONUtils.obj2json(message);
//                        case Ack:
//                            ClientRuntime.CALLED_VALUE_QUEUE.remove(map);
//                            Log.info("Ack:" + JSONUtils.obj2json(message));
//                            return JSONUtils.obj2json(message);
//                        default:
//                            break;
//                    }
                }
            }
            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        } while (System.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS);

        return null;
    }

    /**
     * Get response by messageId
     */
    public static Response callMessageResponse(String messageId) throws InterruptedException, IOException {

        if (Integer.parseInt(messageId) < 0) {
            return ServerRuntime.newResponse(messageId, Constants.booleanString(false), Constants.CMD_NOT_FOUND);
        }

        long timeMillis = System.currentTimeMillis();
        do {
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
        } while (System.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS);

        return ServerRuntime.newResponse(messageId, Constants.booleanString(false), Constants.RESPONSE_TIMEOUT);
    }


    /**
     * Constructing a default Request object
     */
    private static Request defaultRequest() {
        Request request = new Request();
        request.setRequestAck("0");
        request.setSubscriptionEventCounter("0");
        request.setSubscriptionPeriod("0");
        request.setSubscriptionRange("0");
        request.setResponseMaxSize("0");
        request.setRequestMethods(new HashMap<>(16));
        return request;
    }
}
