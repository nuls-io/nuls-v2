package io.nuls.provider.rpctools;

import io.nuls.base.api.provider.Result;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Block;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.provider.utils.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author: PierreLuo
 * @date: 2019-07-23
 */
@Component
public class ConsensusTools implements CallRpc {

    public Object getAgentInfoInContract(int chainId, String agentHash, String contractAddress, String contractSender) {
        Map<String, Object> params = new HashMap(8);
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

    public Map getRandomSeedByCount(int chainId, long height, int count, String algorithm) {
        Map<String, Object> param = new HashMap<>(8);
        param.put("chainId", chainId);
        param.put("height", height);
        param.put("count", count);
        param.put("algorithm", algorithm);
        try {
            return callRpc(ModuleE.CS.abbr, "cs_random_seed_count", param, (Function<Map, Map>) res -> {
                return res;
            });
        } catch (NulsRuntimeException e) {
            throw e;
        }
    }

    public Map getRandomSeedByHeight(int chainId, long startHeight, long endHeight, String algorithm) {
        Map<String, Object> param = new HashMap<>(8);
        param.put("chainId", chainId);
        param.put("startHeight", startHeight);
        param.put("endHeight", endHeight);
        param.put("algorithm", algorithm);
        try {
            return callRpc(ModuleE.CS.abbr, "cs_random_seed_height", param, (Function<Map, Map>) res -> {
                return res;
            });
        } catch (NulsRuntimeException e) {
            throw e;
        }
    }

    public List<String> getRandomRawSeedsByCount(int chainId, long height, int count) {
        Map<String, Object> param = new HashMap<>(8);
        param.put("chainId", chainId);
        param.put("height", height);
        param.put("count", count);
        try {
            return callRpc(ModuleE.CS.abbr, "cs_random_raw_seeds_count", param, (Function<List<String>, List<String>>) res -> {
                return res;
            });
        } catch (NulsRuntimeException e) {
            throw e;
        }
    }

    public List<String> getRandomRawSeedsByHeight(int chainId, long startHeight, long endHeight) {
        Map<String, Object> param = new HashMap<>(8);
        param.put("chainId", chainId);
        param.put("startHeight", startHeight);
        param.put("endHeight", endHeight);
        try {
            return callRpc(ModuleE.CS.abbr, "cs_random_raw_seeds_height", param, (Function<List<String>, List<String>>) res -> {
                return res;
            });
        } catch (NulsRuntimeException e) {
            throw e;
        }
    }

}
