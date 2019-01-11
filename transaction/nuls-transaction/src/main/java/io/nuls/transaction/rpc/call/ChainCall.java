package io.nuls.transaction.rpc.call;

import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.exception.NulsException;

import java.util.HashMap;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: qinyifeng
 * @date: 2018/12/20
 */
public class ChainCall {

    /**
     * 查资产是否存在
     * @param chainId
     * @param assetId
     * @return
     */
    public static boolean verifyAssetExist(int chainId, int assetId) throws NulsException {
        HashMap params = new HashMap();
        params.put("chianId", chainId);
        params.put("assetId", assetId);
        HashMap result = (HashMap) TransactionCall.request("cm_asset", ModuleE.CM.abbr, params);
        if (result.get("assetId") != null) {
            return true;
        }
        return false;
    }

}
