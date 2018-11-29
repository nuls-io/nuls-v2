package io.nuls.account.rpc.call;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.RpcConstant;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: qinyifeng
 * @description: 交易模块接口调用
 * @date: 2018/11/27
 */
public class TransactionCmdCall {

    /**
     * 注册交易
     */
    public static void register() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, RpcConstant.TX_REGISTER_VERSION);
            params.put(RpcConstant.TX_MODULE_CODE, ModuleE.AC.abbr);
            params.put(RpcConstant.TX_MODULE_VALIDATE_CMD, "ac_accountTxValidate");
            params.put(RpcConstant.TX_TYPE, AccountConstant.TX_TYPE_ACCOUNT_ALIAS);
            params.put(RpcConstant.TX_VALIDATE_CMD, "ac_aliasTxValidate");
            params.put(RpcConstant.TX_COMMIT_CMD, "ac_aliasTxCommit");
            params.put(RpcConstant.TX_ROLLBACK_CMD, "ac_rollbackAlias");
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, RpcConstant.TX_REGISTER_CMD, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发起新交易
     */
    public static void newTx(int chainId, String txHex) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, RpcConstant.TX_NEW_VERSION);
            params.put(RpcConstant.TX_CHAIN_ID, chainId);
            params.put(RpcConstant.TX_DATA_HEX, txHex);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, RpcConstant.TX_NEW_CMD, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
