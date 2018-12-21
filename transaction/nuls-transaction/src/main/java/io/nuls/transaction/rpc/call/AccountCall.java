package io.nuls.transaction.rpc.call;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: qinyifeng
 * @date: 2018/12/20
 */
public class AccountCall {

    public static String getPrikey(String address, String password) {
        /**
         * 查询地址私钥
         * Query address private key
         */
        int chainId = AddressTool.getChainIdByAddress(address);
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("address", address);
        params.put("password", password);
        try {
            HashMap result = (HashMap) TransactionCall.request(ModuleE.AC.abbr, "ac_getPriKeyByAddress", params);
            return (String) result.get("priKey");
        } catch (Exception e) {
            Log.error(e);
        }
        return null;
    }

    /**
     * 查询多签账户
     * Query multi-sign account
     *
     * @param multiSignAddress
     * @return
     */
    public static MultiSigAccount getMultiSigAccount(byte[] multiSignAddress) {
        MultiSigAccount multiSigAccount = null;
        String address = AddressTool.getStringAddressByBytes(multiSignAddress);
        int chainId = AddressTool.getChainIdByAddress(address);
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chainId);
        params.put("address", address);
        try {
            Object result = TransactionCall.request(ModuleE.AC.abbr, "ac_getMultiSigAccount", params);
            multiSigAccount = JSONUtils.json2pojo(JSONUtils.obj2json(result), MultiSigAccount.class);
        } catch (Exception e) {
            Log.error(e);
        }
        return multiSigAccount;
    }

}
