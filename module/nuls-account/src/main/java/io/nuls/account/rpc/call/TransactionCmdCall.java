package io.nuls.account.rpc.call;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.RpcConstant;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.log.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: qinyifeng
 * @description: 交易模块接口调用
 * @date: 2018/11/27
 */
public class TransactionCmdCall {

    /**
     * 发起新交易
     */
    public static boolean newTx(int chainId, String txStr) {
        try {
            Map<String, Object> params = new HashMap<>(AccountConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, RpcConstant.TX_NEW_VERSION);
            params.put(RpcConstant.TX_CHAIN_ID, chainId);
            params.put(RpcConstant.TX_DATA, txStr);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, RpcConstant.TX_NEW_CMD, params);
            return cmdResp.isSuccess();
        } catch (Exception e) {
            Log.error("", e);
            return false;
        }
    }

    /**
     * 交易基础验证
     */
    public static boolean baseValidateTx(int chainId, String txStr) {
        try {
            Map<String, Object> params = new HashMap<>(AccountConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, RpcConstant.TX_NEW_VERSION);
            params.put(RpcConstant.TX_CHAIN_ID, chainId);
            params.put(RpcConstant.TX_DATA, txStr);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, RpcConstant.TX_BASE_VALIDATE, params);
            if (!cmdResp.isSuccess()) {
                return false;
            }
            HashMap hashMap = (HashMap)((HashMap) cmdResp.getResponseData()).get("tx_baseValidateTx");
            return (boolean) hashMap.get("value");
        } catch (Exception e) {
            Log.error("", e);
            return false;
        }
    }

}
