package io.nuls.rpc.cmd;

import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.CmdRequest;
import io.nuls.rpc.model.Module;
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
     * 1. send local module information to kernel
     * 2. receive all the modules' interfaces from kernel
     */
    public static void syncKernel(String kernelUri) throws Exception {
        int id = RuntimeInfo.nextSequence();
        CmdRequest cmdRequest = new CmdRequest(id, "version", 1.0, new Object[]{RuntimeInfo.local});

        WsClient wsClient = RuntimeInfo.getWsClient(kernelUri);
        wsClient.send(JSONUtils.obj2json(cmdRequest));

        Map rspMap = wsClient.getResponse(id);

        Map resultMap = (Map) rspMap.get("result");
        if (resultMap == null) {
            return;
        }

        if (RuntimeInfo.local != null) {
            RuntimeInfo.local.setAvailable((Boolean) resultMap.get("available"));
        }

        //Map<String, Object> moduleMap = JSONUtils.json2map(JSONUtils.obj2json(resultMap.get("modules")));
        @SuppressWarnings("unchecked")
        Map<String, Object> moduleMap = (Map<String, Object>) resultMap.get("modules");
        Log.info(JSONUtils.obj2json(moduleMap));
        for (String key : moduleMap.keySet()) {
            Module module = JSONUtils.json2pojo(JSONUtils.obj2json(moduleMap.get(key)), Module.class);
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
        CmdRequest cmdRequest = new CmdRequest(id, cmd, minVersion, params);

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
