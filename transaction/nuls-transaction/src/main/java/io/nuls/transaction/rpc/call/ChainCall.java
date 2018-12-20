package io.nuls.transaction.rpc.call;

import io.nuls.base.data.Transaction;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.model.bo.Chain;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: qinyifeng
 * @date: 2018/12/05
 */
public class ChainCall {

    public static boolean assetExist(int chainId, int assetId) {
        //查资产是否存在
        HashMap params = new HashMap();
        params.put("chianId", chainId);
        params.put("assetId", assetId);
        HashMap result = (HashMap)TransactionCall.request("cmd", ModuleE.CM.abbr, params);
        return (Boolean) result.get("value");
    }

}
