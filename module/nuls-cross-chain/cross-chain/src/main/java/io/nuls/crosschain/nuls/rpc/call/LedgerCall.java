package io.nuls.crosschain.nuls.rpc.call;

import io.nuls.base.basic.AddressTool;
import io.nuls.core.parse.JSONUtils;
import io.nuls.crosschain.base.message.CirculationMessage;
import io.nuls.crosschain.base.model.bo.Circulation;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.BigIntegerUtils;
import static io.nuls.crosschain.nuls.constant.NulsCrossChainConstant.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: tag
 * @date: 2019/4/12
 */
public class LedgerCall {

    /**
     * 查询nonce值
     *
     * @param chain
     * @param address
     * @param assetChainId
     * @param assetId
     * @return
     * @throws NulsException
     */
    public static byte[] getNonce(Chain chain, String address, int assetChainId, int assetId) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("address", address);
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            HashMap result = (HashMap) CommonCall.request(ModuleE.LG.abbr, "getNonce", params);
            String nonce = (String) result.get("nonce");
            return RPCUtil.decode(nonce);
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }


    /**
     * 查询账户特定资产的余额(包含未确认的余额)
     * Check the balance of an account-specific asset
     */
    public static BigInteger getBalanceNonce(Chain chain, byte[] address, int assetChainId, int assetId) throws NulsException {
        try {
            String addressString = AddressTool.getStringAddressByBytes(address);
            Map<String, Object> params = new HashMap<>(INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            params.put("address", addressString);
            Map result = (Map)CommonCall.request(ModuleE.LG.abbr, "getBalanceNonce", params);
            Object available = result.get("available");
            return BigIntegerUtils.stringToBigInteger(String.valueOf(available));
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 获取可用余额和nonce
     * Get the available balance and nonce
     *
     * @param chain
     * @param address
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getBalanceAndNonce(Chain chain, byte[] address, int assetChainId, int assetId) throws NulsException {
        String addressString = AddressTool.getStringAddressByBytes(address);
        Map<String, Object> params = new HashMap<>(INIT_CAPACITY_8);
        params.put(Constants.VERSION_KEY_STR, RPC_VERSION);
        params.put("chainId", chain.getChainId());
        params.put("assetChainId", assetChainId);
        params.put("assetId", assetId);
        params.put("address", addressString);
        try {
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBalanceNonce", params);
            if (!callResp.isSuccess()) {
                return null;
            }
            return (HashMap) ((HashMap) callResp.getResponseData()).get("getBalanceNonce");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }


    /**
     * 查询账户特定资产的余额(只获取已确认的余额)
     * Check the balance of an account-specific asset
     */
    public static BigInteger getBalance(Chain chain, byte[] address, int assetChainId, int assetId) throws NulsException {
        try {
            String addressString = AddressTool.getStringAddressByBytes(address);
            Map<String, Object> params = new HashMap<>(INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            params.put("address", addressString);
            Map result = (Map)CommonCall.request(ModuleE.LG.abbr, "getBalance", params);
            Object available = result.get("available");
            return BigIntegerUtils.stringToBigInteger(String.valueOf(available));
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 查询资产流通量
     * Search for assets circulation
     * */
    @SuppressWarnings("unchecked")
    public static List<Circulation> getAssetsById(Chain chain, String assetIds ) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.VERSION_KEY_STR, RPC_VERSION);
            params.put("chainId", chain.getChainId());
            params.put("assetIds", assetIds);
            Map result = (Map)CommonCall.request(ModuleE.LG.abbr, "getAssetsById", params);
            List<Circulation> circulationList = new ArrayList<>();
            List<Map<String,Object>> assertList = (List<Map<String,Object>> )result.get("assets");
            if(assertList != null && assertList.size() > 0){
                for (Map<String,Object> assertInfoMap:assertList) {
                    Circulation circulation = JSONUtils.map2pojo(assertInfoMap, Circulation.class);
                    circulationList.add(circulation);
                }
            }
            return circulationList;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }
}
