package io.nuls.rpc.cmd;

import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.model.CmdRequest;
import io.nuls.rpc.model.Module;
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
     * send local module information to kernel
     */
    public static void syncLocalToKernel(String kernelUri) throws Exception {
        int id = RuntimeInfo.nextSequence();
        CmdRequest cmdRequest = new CmdRequest(id, "version", 1.0, new Object[]{RuntimeInfo.local});
        WsClient wsClient = RuntimeInfo.getWsClient(kernelUri);

        wsClient.send(JSONUtils.obj2json(cmdRequest));
        Map remoteMap = wsClient.wsResponse(id);

        Map resultMap = (Map) remoteMap.get("result");
        RuntimeInfo.local.setAvailable((Boolean) resultMap.get("available"));

        Map<String, Object> moduleMap = JSONUtils.json2map(JSONUtils.obj2json(resultMap.get("modules")));
        for (String key : moduleMap.keySet()) {
            Module module = JSONUtils.json2pojo(JSONUtils.obj2json(moduleMap.get(key)), Module.class);
            RuntimeInfo.remoteModuleMap.put(key, module);
        }
    }

    public static String call(String cmd, Object[] params, double minVersion) throws Exception {
        int id = RuntimeInfo.sequence.incrementAndGet();
        CmdRequest cmdRequest = new CmdRequest(id, cmd, minVersion, params);

        List<String> remoteUriList = RuntimeInfo.getRemoteUri(cmdRequest);
        if (remoteUriList.size() == 0) {
            return "No cmd found->" + cmd + "." + minVersion;
        }
        if (remoteUriList.size() > 1) {
            return "Multiply cmd found->" + cmd;
        }

        String remoteUri = remoteUriList.get(0);
        WsClient wsClient = RuntimeInfo.getWsClient(remoteUri);
        wsClient.send(JSONUtils.obj2json(cmdRequest));
        Map remoteMap = wsClient.wsResponse(id);
        return JSONUtils.obj2json(remoteMap);
    }
}
