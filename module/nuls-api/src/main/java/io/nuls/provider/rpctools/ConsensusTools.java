package io.nuls.provider.rpctools;

import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author: PierreLuo
 * @date: 2019-07-23
 */
@Component
public class ConsensusTools implements CallRpc {

    public Object getAgentInfoInContract(int chainId, String agentHash, String contractAddress, String contractSender) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("agentHash", agentHash);
        params.put("contractAddress", contractAddress);
        params.put("contractSender", contractSender);
        try {
            return callRpc(ModuleE.CS.abbr, "cs_getContractAgentInfo", params, (Function<Object, Object>) obj -> {
                return obj;
            });
        } catch (NulsRuntimeException e) {
            throw e;
        }
    }


}
