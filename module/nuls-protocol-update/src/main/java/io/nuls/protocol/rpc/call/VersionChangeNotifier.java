package io.nuls.protocol.rpc.call;

import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.protocol.Protocol;
import io.nuls.core.rpc.util.RegisterHelper;
import io.nuls.core.rpc.util.RpcCall;
import io.nuls.protocol.model.ProtocolContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionChangeNotifier {

    /**
     * 协议升级后通知各模块
     * @param chainId
     * @param version
     * @return
     */
    public static boolean notify(int chainId, short version) {
        String[] modules = new String[]{
                ModuleE.CS.abbr,
                ModuleE.BL.abbr,
                ModuleE.AC.abbr,
                ModuleE.TX.abbr
        };
        for (String module : modules) {
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("protocolVersion", version);
            try {
                RpcCall.request(module, "protocolVersionChange", params);
            } catch (NulsException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 协议版本变化时,重新注册交易、消息
     * @param chainId
     * @param context
     * @param version
     * @return
     */
    public static boolean reRegister(int chainId, ProtocolContext context, short version) {
        List<Map.Entry<String, Protocol>> entries = context.getProtocolMap().get(version);
        if (entries != null) {
            entries.forEach(e -> {
                RegisterHelper.registerMsg(e.getValue(), e.getKey());
                RegisterHelper.registerTx(chainId, e.getValue(), e.getKey());
            });
        }
        return true;
    }
}
