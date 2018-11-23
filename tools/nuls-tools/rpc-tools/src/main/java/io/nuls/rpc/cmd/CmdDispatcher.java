package io.nuls.rpc.cmd;

import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.handler.CmdHandler;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.ModuleInfo;
import io.nuls.rpc.model.message.*;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

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
        int messageId = RuntimeInfo.nextSequence();
        Message message = CmdHandler.basicMessage(messageId, MessageType.NegotiateConnection);
        message.setMessageData(CmdHandler.defaultNegotiateConnection());

        WsClient wsClient = RuntimeInfo.getWsClient(RuntimeInfo.kernelUrl);
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
     */
    public static void syncKernel() throws Exception {
        int messageId = RuntimeInfo.nextSequence();
        Message message = CmdHandler.basicMessage(messageId, MessageType.Request);
        Request request = CmdHandler.defaultRequest();
        request.getRequestMethods().put("registerAPI", RuntimeInfo.local.getRegisterApi());
        message.setMessageData(request);

        WsClient wsClient = RuntimeInfo.getWsClient(RuntimeInfo.kernelUrl);
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
            RuntimeInfo.remoteModuleMap.put(key, moduleInfo);
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
    public static String request(String cmd, Map params) throws Exception {
        int messageId = request(cmd, params, 0);
        return callValue(messageId);
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
        int messageId = RuntimeInfo.sequence.incrementAndGet();
        Message message = CmdHandler.basicMessage(messageId, MessageType.Request);
        Request request = CmdHandler.defaultRequest();
        request.setSubscriptionPeriod(subscriptionPeriod);
        request.getRequestMethods().put(cmd, params);
        message.setMessageData(request);

        String uri = RuntimeInfo.getRemoteUri(cmd);
        if (uri == null) {
            return -1;
        }
        WsClient wsClient = RuntimeInfo.getWsClient(uri);
        Log.info("Request:" + JSONUtils.obj2json(message));
        wsClient.send(JSONUtils.obj2json(message));

        return messageId;
    }

    /**
     * Method of Unsubscribe
     * A call that responds only once does not need to be cancelled
     *
     * @param messageId Request message ID
     * @param cmd       Request command
     */
    public static void unsubscribe(int messageId, String cmd) throws Exception {
        Message message = CmdHandler.basicMessage(messageId, MessageType.Unsubscribe);
        Unsubscribe unsubscribe = new Unsubscribe();
        unsubscribe.setUnsubscribeMethods(new String[]{messageId + cmd});
        message.setMessageData(unsubscribe);

        String uri = RuntimeInfo.getRemoteUri(cmd);
        if (uri != null) {
            WsClient wsClient = RuntimeInfo.getWsClient(uri);
            wsClient.send(JSONUtils.obj2json(message));
        }
    }

    /**
     * Get response by messageId
     */
    public static String callValue(int messageId) throws InterruptedException, IOException {

        if (messageId < 0) {
            Response response = CmdHandler.defaultResponse(messageId, Constants.RESPONSE_STATUS_FAILED, Constants.CMD_NOT_FOUND);
            return JSONUtils.obj2json(response);
        }

        long timeMillis = System.currentTimeMillis();
        do {
            synchronized (RuntimeInfo.CALLED_VALUE_QUEUE) {
                for (Map map : RuntimeInfo.CALLED_VALUE_QUEUE) {
                    Message message = JSONUtils.map2pojo(map, Message.class);
                    MessageType messageType = MessageType.valueOf(message.getMessageType());
                    switch (messageType) {
                        case NegotiateConnectionResponse:
                            RuntimeInfo.CALLED_VALUE_QUEUE.remove(map);
                            Log.info("NegotiateConnectionResponse:" + JSONUtils.obj2json(map));
                            return JSONUtils.obj2json(map);
                        case Response:
                            Response response = JSONUtils.map2pojo((Map) message.getMessageData(), Response.class);
                            if (response.getRequestId() == messageId) {
                                RuntimeInfo.CALLED_VALUE_QUEUE.remove(map);
                                Log.info("Response:" + JSONUtils.obj2json(response));
                                return JSONUtils.obj2json(response);
                            }
                            break;
                        case Ack:
                            RuntimeInfo.CALLED_VALUE_QUEUE.remove(map);
                            Log.info("Ack:" + JSONUtils.obj2json(map));
                            return JSONUtils.obj2json(map);
                        default:
                            break;
                    }
                }
            }
            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        } while (System.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS);

        return JSONUtils.obj2json(CmdHandler.defaultResponse(messageId, Constants.RESPONSE_STATUS_FAILED, Constants.RESPONSE_TIMEOUT));
    }


}
