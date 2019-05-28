package io.nuls.protocol.rpc.call;

import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.util.ModuleHelper;
import io.nuls.core.rpc.util.RpcCall;

import java.util.ArrayList;
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
        List<String> noticedModule = new ArrayList<>();
        noticedModule.add(ModuleE.CS.abbr);
        noticedModule.add(ModuleE.BL.abbr);
        noticedModule.add(ModuleE.AC.abbr);
        noticedModule.add(ModuleE.TX.abbr);
        if (ModuleHelper.isSupportSmartContract()) {
            noticedModule.add(ModuleE.SC.abbr);
        }
        if (ModuleHelper.isSupportCrossChain()) {
            noticedModule.add(ModuleE.CC.abbr);
            noticedModule.add(ModuleE.CM.abbr);
        }
        for (String module : noticedModule) {
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
}
