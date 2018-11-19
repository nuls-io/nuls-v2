package io.nuls.rpc.cmd;

import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.CmdRequest;
import io.nuls.rpc.model.ModuleInfo;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.Request;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.util.List;
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
        Message message = RuntimeInfo.buildMessage(messageId);
        message.setMessageType(MessageType.NegotiateConnection.name());
        message.setMessageData(RuntimeInfo.buildNegotiateConnection());

        WsClient wsClient = RuntimeInfo.getWsClient(RuntimeInfo.kernelUrl);
        if (wsClient == null) {
            throw new Exception("Kernel not available");
        }
        wsClient.send(JSONUtils.obj2json(message));

        Map rspMap = wsClient.getResponse(messageId);
        return MessageType.NegotiateConnectionResponse.name().equals(rspMap.get("messageType"));
    }

    /**
     * 1. send local module information to kernel
     * 2. receive all the modules' interfaces from kernel
     */
    public static void syncKernel() throws Exception {
        int messageId = RuntimeInfo.nextSequence();
        Message message=RuntimeInfo.buildMessage(messageId);
        message.setMessageType(MessageType.Request.name());
        Request request=new Request();
        request.setRequestAck(0);
        request.setSubscriptionEventCounter(0);
        request.setSubscriptionPeriod(0);
        request.setSubscriptionRange("");
        request.setResponseMaxSize(0);
//        request.setRequestMethods();
        message.setMessageData(request);

        WsClient wsClient = RuntimeInfo.getWsClient(RuntimeInfo.kernelUrl);
        if (wsClient == null) {
            throw new Exception("Kernel not available");
        }
        wsClient.send(JSONUtils.obj2json(message));

        Map rspMap = wsClient.getResponse(messageId);

        Map resultMap = (Map) rspMap.get("result");
        if (resultMap == null) {
            return;
        }

        //Map<String, Object> moduleMap = JSONUtils.json2map(JSONUtils.obj2json(resultMap.get("modules")));
        @SuppressWarnings("unchecked")
        Map<String, Object> moduleMap = (Map<String, Object>) resultMap.get("modules");
        Log.info(JSONUtils.obj2json(moduleMap));
        for (String key : moduleMap.keySet()) {
            ModuleInfo module = JSONUtils.json2pojo(JSONUtils.obj2json(moduleMap.get(key)), ModuleInfo.class);
            RuntimeInfo.remoteModuleMap.put(key, module);
        }
    }

    /**
     * call cmd.
     * 1. Find the corresponding module according to cmd
     * 2. Send to the specified module
     * 3. Get the result returned to the caller
     */
    public static String call(String cmd, Object[] params, double minVersion) throws Exception {
        int id = RuntimeInfo.sequence.incrementAndGet();
        if (params == null) {
            params = new Object[]{};
        }
        CmdRequest cmdRequest = new CmdRequest(id, cmd, minVersion, params);

        return response(id, cmdRequest);
    }

    /**
     * call cmd.
     * 1. Find the corresponding module according to cmd
     * 2. Send to the specified module
     * 3. Get the result returned to the caller
     * 4. Get the highest version of cmd
     */
    public static String call(String cmd, Object[] params) throws Exception {
        int id = RuntimeInfo.sequence.incrementAndGet();
        if (params == null) {
            params = new Object[]{};
        }
        CmdRequest cmdRequest = new CmdRequest(id, cmd, -1, params);

        return response(id, cmdRequest);
    }

    private static String response(int id, CmdRequest cmdRequest) throws Exception {
        List<String> remoteUriList = RuntimeInfo.getRemoteUri(cmdRequest);
        switch (remoteUriList.size()) {
            case 0:
                return JSONUtils.obj2json(RuntimeInfo.buildCmdResponseMap(id, Constants.CMD_NOT_FOUND));
            case 1:
                String remoteUri = remoteUriList.get(0);
                WsClient wsClient = RuntimeInfo.getWsClient(remoteUri);
                wsClient.send(JSONUtils.obj2json(cmdRequest));
                Map remoteMap = wsClient.getResponse(id);
                return JSONUtils.obj2json(remoteMap);
            default:
                return JSONUtils.obj2json(RuntimeInfo.buildCmdResponseMap(id, Constants.CMD_DUPLICATE));
        }
    }
}
