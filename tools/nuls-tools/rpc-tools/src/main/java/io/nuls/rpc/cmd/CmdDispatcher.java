package io.nuls.rpc.cmd;

import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.ModuleInfo;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.Request;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.util.Map;

/**
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
        Message message = RuntimeInfo.buildMessage(messageId, MessageType.NegotiateConnection);
        message.setMessageData(RuntimeInfo.defaultNegotiateConnection());

        WsClient wsClient = RuntimeInfo.getWsClient(RuntimeInfo.kernelUrl);
        if (wsClient == null) {
            throw new Exception("Kernel not available");
        }
        wsClient.send(JSONUtils.obj2json(message));

        Map rspMap = (Map) wsClient.getResponse(messageId);
        return MessageType.NegotiateConnectionResponse.name().equals(rspMap.get("messageType"));
    }

    /**
     * 1. send local module information to kernel
     * 2. receive all the modules' interfaces from kernel
     */
    public static void syncKernel() throws Exception {
        int messageId = RuntimeInfo.nextSequence();
        Message message = RuntimeInfo.buildMessage(messageId, MessageType.Request);
        Request request = RuntimeInfo.defaultRequest();
        request.getRequestMethods().put("registerAPI", RuntimeInfo.local.getRegisterApi());
        message.setMessageData(request);

        WsClient wsClient = RuntimeInfo.getWsClient(RuntimeInfo.kernelUrl);
        if (wsClient == null) {
            throw new Exception("Kernel not available");
        }
        wsClient.send(JSONUtils.obj2json(message));

        @SuppressWarnings("unchecked")
        Map<String, Object> responseData = (Map<String, Object>) wsClient.getResponse(messageId);
        Log.info("APIMethods from kernel:" + JSONUtils.obj2json(responseData));
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
     */
    public static String request(String cmd, Object[] params, double minVersion) throws Exception {
        int messageId = RuntimeInfo.sequence.incrementAndGet();
        Message message = RuntimeInfo.buildMessage(messageId, MessageType.Request);
        Request request = RuntimeInfo.defaultRequest();
        request.getRequestMethods().put(cmd, params);

        String uri = RuntimeInfo.getRemoteUri(cmd);
        WsClient wsClient = RuntimeInfo.getWsClient(uri);

        wsClient.send(JSONUtils.obj2json(message));
        Object response = wsClient.getResponse(messageId);

        return JSONUtils.obj2json(response);
    }

    /**
     * call cmd.
     * 1. Find the corresponding module according to cmd
     * 2. Send to the specified module
     * 3. Get the result returned to the caller
     * 4. Get the highest version of cmd
     */
    public static String request(String cmd, Map params) throws Exception {
        int messageId = RuntimeInfo.sequence.incrementAndGet();
        Message message = RuntimeInfo.buildMessage(messageId, MessageType.Request);
        Request request = RuntimeInfo.defaultRequest();
        request.getRequestMethods().put(cmd, params);
        message.setMessageData(request);

        String uri = RuntimeInfo.getRemoteUri(cmd);
        if (uri == null) {
            return "No cmd found:" + cmd;
        }
        WsClient wsClient = RuntimeInfo.getWsClient(uri);

        wsClient.send(JSONUtils.obj2json(message));
        Object response = wsClient.getResponse(messageId);

        return JSONUtils.obj2json(response);
    }

//    private static String response(int id, CmdRequest cmdRequest) throws Exception {
//        List<String> remoteUriList = RuntimeInfo.getRemoteUri(cmdRequest);
//        switch (remoteUriList.size()) {
//            case 0:
//                return JSONUtils.obj2json(RuntimeInfo.buildCmdResponseMap(id, Constants.CMD_NOT_FOUND));
//            case 1:
//                String remoteUri = remoteUriList.get(0);
//                WsClient wsClient = RuntimeInfo.getWsClient(remoteUri);
//                wsClient.send(JSONUtils.obj2json(cmdRequest));
//                Map remoteMap = wsClient.getResponse(id);
//                return JSONUtils.obj2json(remoteMap);
//            default:
//                return JSONUtils.obj2json(RuntimeInfo.buildCmdResponseMap(id, Constants.CMD_DUPLICATE));
//        }
//    }
}
