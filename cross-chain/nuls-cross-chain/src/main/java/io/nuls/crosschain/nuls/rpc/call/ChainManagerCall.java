package io.nuls.crosschain.nuls.rpc.call;

import io.nuls.base.data.Transaction;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.util.RPCUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import static io.nuls.crosschain.nuls.constant.NulsCrossChainConstant.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 与跨链管理模块交易接口
 *
 * @author: tag
 * @date: 2019/4/15
 */
public class ChainManagerCall {
    /**
     * 验证跨链交易资产
     * @param tx
     * @return boolean
     * @throws NulsException
     */
    @SuppressWarnings("unchecked")
    public static boolean verifyCtxAsset(int chainId, Transaction tx) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, RPC_VERSION);
            params.put("chainId", chainId);
            params.put("tx", RPCUtil.encode(tx.serialize()));
            HashMap result = (HashMap) CommonCall.request(ModuleE.CM.abbr,"cm_assetCirculateValidator",  params);
            return (boolean) result.get("value");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 跨链交易链资产管理提交
     * @param txList
     * @param blockHeader
     * @return
     * @throws NulsException
     */
    @SuppressWarnings("unchecked")
    public static boolean ctxAssetCirculateCommit(int chainId,List<String> txList, String blockHeader) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, RPC_VERSION);
            params.put("chainId", chainId);
            params.put("txList", txList);
            params.put("blockHeaderDigest", blockHeader);
            HashMap result = (HashMap) CommonCall.request(ModuleE.CM.abbr,"cm_assetCirculateCommit", params);
            return (boolean) result.get("value");
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * 跨链交易链资产管理回滚
     * @param txList
     * @param blockHeader
     * @return
     * @throws NulsException
     */
    @SuppressWarnings("unchecked")
    public static boolean ctxAssetCirculateRollback(int chainId,List<String> txList, String blockHeader) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, RPC_VERSION);
            params.put("chainId", chainId);
            params.put("txList", txList);
            params.put("blockHeaderDigest", blockHeader);
            HashMap result = (HashMap) CommonCall.request(ModuleE.CM.abbr,"cm_assetCirculateRollBack", params);
            return (boolean) result.get("value");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }
}
