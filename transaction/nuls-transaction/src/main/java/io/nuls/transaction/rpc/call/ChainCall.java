package io.nuls.transaction.rpc.call;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.exception.NulsException;
import static io.nuls.transaction.utils.LoggerUtil.Log;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: qinyifeng
 * @date: 2018/12/20
 */
public class ChainCall {


    /**
     * 验证跨链交易资产
     * @param txHex
     * @return boolean
     * @throws NulsException
     */
    public static boolean verifyCtxAsset(Chain chain, String txHex) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chain.getChainId());
            params.put("txHex", txHex);
            HashMap result = (HashMap) TransactionCall.request(ModuleE.CM.abbr,"cm_assetCirculateValidator",  params);
            return (boolean) result.get("value") == true;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 跨链交易链资产管理提交
     * @param chain
     * @param txHexList
     * @param blockHeaderHex
     * @return
     * @throws NulsException
     */
    public static boolean ctxAssetCirculateCommit(Chain chain, List<String> txHexList, String blockHeaderHex) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chain.getChainId());
            params.put("txHexList", txHexList);
            params.put("blockHeaderDigest", blockHeaderHex);
            HashMap result = (HashMap) TransactionCall.request(ModuleE.CM.abbr,"cm_assetCirculateCommit", params);
            return (boolean) result.get("value") == true;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * 跨链交易链资产管理回滚
     * @param chain
     * @param txHexList
     * @param blockHeaderHex
     * @return
     * @throws NulsException
     */
    public static boolean ctxAssetCirculateRollback(Chain chain, List<String> txHexList, String blockHeaderHex) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chain.getChainId());
            params.put("txHexList", txHexList);
            params.put("blockHeaderDigest", blockHeaderHex);
            HashMap result = (HashMap) TransactionCall.request(ModuleE.CM.abbr,"cm_assetCirculateRollBack", params);
            return (boolean) result.get("value") == true;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

}
