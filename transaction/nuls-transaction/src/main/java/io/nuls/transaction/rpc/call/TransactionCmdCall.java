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
     * 调用其他模块接口
     * Call other module interfaces
     */
    public static HashMap request(String cmd,String moduleCode,Map params) {
        HashMap result = new HashMap();
        try {
            params.put(Constants.VERSION_KEY_STR, "1.0");
            Response cmdResp = CmdDispatcher.requestAndResponse(moduleCode, cmd, params);
            if(cmdResp.isSuccess()) {
                result = (HashMap) ((HashMap) cmdResp.getResponseData()).get(cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
