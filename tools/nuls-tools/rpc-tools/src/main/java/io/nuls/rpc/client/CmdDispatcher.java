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

        return getNegotiateConnectionResponse();
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
     * 1. Send request
     * 2. Get messageId
     * 3. Obtain Response Based on MessageId
     */
    public static Response requestAndResponse(String role, String cmd, Map params) throws Exception {
        String messageId = request(role, cmd, params, Constants.booleanString(false), "0");
        return getResponse(messageId);
    }

    /**
     * 1. Send request
     * 2. Get messageId
     * 3. Put the messageId to INVOKE_MAP
     * End. The requester can handle other things
     * A separate thread listens for new messages.
     * Messages are automatically sent to the correct method
     */
    public static String requestAndInvoke(String role, String cmd, Map params, String subscriptionPeriod, Class clazz, String invokeMethod) throws Exception {
        if (Integer.parseInt(subscriptionPeriod) <= 0) {
            throw new Exception("subscriptionPeriod must great than 0");
        }
        String messageId = request(role, cmd, params, Constants.booleanString(false), subscriptionPeriod);
        ClientRuntime.INVOKE_MAP.put(messageId, new Object[]{clazz, invokeMethod});
        return messageId;
    }

    /**
     * The same as requestAndInvoke
     * The difference is that messageId will be returned only when Ack is true .
     */
    public static String requestAndInvokeWithAck(String role, String cmd, Map params, String subscriptionPeriod, Class clazz, String invokeMethod) throws Exception {
        String messageId = request(role, cmd, params, Constants.booleanString(true), subscriptionPeriod);
        ClientRuntime.INVOKE_MAP.put(messageId, new Object[]{clazz, invokeMethod});
        return getAck(messageId) ? messageId : null;
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
    private static boolean getNegotiateConnectionResponse() throws InterruptedException, IOException {

        long timeMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS) {
            /*
            Get the first item of the queue
            If it is an empty object, discard
             */
            Map map = ClientRuntime.firstItemInServerResponseQueue();
            if (map == null) {
                continue;
            }

            /*
            Message type should be "NegotiateConnectionResponse"
            If not NegotiateConnectionResponse, add back to the queue and wait for other threads to process
             */
            Message message = JSONUtils.map2pojo(map, Message.class);
            if (MessageType.NegotiateConnectionResponse.name().equals(message.getMessageType())) {
                Log.info("NegotiateConnectionResponse:" + JSONUtils.obj2json(message));
                return true;
            } else {
                ClientRuntime.SERVER_RESPONSE_QUEUE.add(map);
            }

            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        }

        /*
        Timeout Error
         */
        return false;
    }

    /**
     * Get response by messageId
     */
    private static Response getResponse(String messageId) throws InterruptedException, IOException {
        if (Integer.parseInt(messageId) < 0) {
            return ServerRuntime.newResponse(messageId, Constants.booleanString(false), Constants.CMD_NOT_FOUND);
        }

        long timeMillis = System.currentTimeMillis();
        while (System.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS) {
            /*
            Get the first item of the queue
            If it is an empty object, discard
             */
            Map map = ClientRuntime.firstItemInServerResponseQueue();
            if (map == null) {
                continue;
            }

            /*
            Message type should be "Response"
            If not Response, add back to the queue and wait for other threads to process
             */
            Message message = JSONUtils.map2pojo(map, Message.class);
            if (!MessageType.Response.name().equals(message.getMessageType())) {
                ClientRuntime.SERVER_RESPONSE_QUEUE.add(map);
                continue;
            }

            Response response = JSONUtils.map2pojo((Map) message.getMessageData(), Response.class);
            if (response.getRequestId().equals(messageId)) {
                /*
                If messageId is the same, then the response is needed
                 */
                Log.info("Response:" + JSONUtils.obj2json(message));
                return response;
            } else {
                /*
                Add back to the queue and wait for other threads to process
                 */
                ClientRuntime.SERVER_RESPONSE_QUEUE.add(map);
            }

            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        }

        /*
        Timeout Error
         */
        return ServerRuntime.newResponse(messageId, Constants.booleanString(false), Constants.RESPONSE_TIMEOUT);
    }

    /**
     * Get ack by messageId
     */
    private static boolean getAck(String messageId) throws InterruptedException, IOException {
        if (Integer.parseInt(messageId) < 0) {
            return false;
        }

        long timeMillis = TimeService.currentTimeMillis();
        while (TimeService.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS) {
            /*
            Get the first item of the queue
            If it is an empty object, discard
             */
            Map map = ClientRuntime.firstItemInServerResponseQueue();
            if (map == null) {
                continue;
            }

            /*
            Message type should be "Ack"
            If not Ack, add back to the queue and wait for other threads to process
             */
            Message message = JSONUtils.map2pojo(map, Message.class);
            if (!MessageType.Ack.name().equals(message.getMessageType())) {
                ClientRuntime.SERVER_RESPONSE_QUEUE.add(map);
                continue;
            }

            Ack ack = JSONUtils.map2pojo((Map) message.getMessageData(), Ack.class);
            if (ack.getRequestId().equals(messageId)) {
                /*
                If messageId is the same, then the ack is needed
                 */
                Log.info("Ack:" + JSONUtils.obj2json(ack));
                return true;
            } else {
                /*
                Add back to the queue and wait for other threads to process
                 */
                ClientRuntime.SERVER_RESPONSE_QUEUE.add(map);
            }

            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        }

        /*
        Timeout Error
         */
        return false;
    }

}
