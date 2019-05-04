package io.nuls.protocol.rpc.call;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.util.RpcCall;
import io.nuls.core.exception.NulsException;

import java.util.HashMap;
import java.util.Map;

public class VersionChangeNotifier {
    public static boolean notify(int chainId, short version) {
        String[] modules = new String[]{
                ModuleE.CS.abbr,
                ModuleE.BL.abbr,
                ModuleE.TX.abbr
        };
        for (String module : modules) {
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
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
