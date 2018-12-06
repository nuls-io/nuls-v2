package io.nuls.transaction.rpc.call;

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 * @author: qinyifeng
 * @date: 2018/12/05
 */
public class TransactionCmdCall {

    /**
     * 交易确认提交
     * Transaction processor commit
     */
    public static boolean txCommit(String cmd,String moduleCode,Map params) {
        try {
            params.put(Constants.VERSION_KEY_STR, "1.0");
            Response cmdResp = CmdDispatcher.requestAndResponse(moduleCode, cmd, params);
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get(cmd);
            Boolean value = (Boolean) result.get("value");
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
