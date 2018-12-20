package io.nuls.transaction.rpc.call;

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Response;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: qinyifeng
 * @date: 2018/12/05
 */
public class TransactionCall {

    /**
     * 调用其他模块接口
     * Call other module interfaces
     */
    public static Object request(String cmd, String moduleCode, Map params) {
        HashMap result = new HashMap();
        try {
            params.put(Constants.VERSION_KEY_STR, "1.0");
            Response cmdResp = CmdDispatcher.requestAndResponse(moduleCode, cmd, params);
            if (cmdResp.isSuccess()) {
                result = (HashMap) ((HashMap) cmdResp.getResponseData()).get(cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean txValidator(Chain chain, String cmd, String moduleCode, String txHex) {
        //调用单个交易验证器
        HashMap params = new HashMap();
        params.put("chianId", chain.getChainId());
        params.put("txHex", txHex);
        HashMap result = (HashMap)TransactionCall.request(cmd, moduleCode, params);
        return (Boolean) result.get("value");
    }

    public static boolean txsModuleValidators(Chain chain, Map<TxRegister, List<String>> map) {
        //调用交易模块统一验证器 批量
        boolean rs = true;
        for (Map.Entry<TxRegister, List<String>> entry : map.entrySet()) {
            List<String> list = txModuleValidator(chain, entry.getKey().getModuleValidator(), entry.getKey().getModuleCode(), entry.getValue());
            if (list.size() > 0) {
                rs = false;
                break;
            }
        }
        return rs;
    }

    /**
     * 统一验证返回被干掉的交易hash
     *
     * @param moduleValidator
     * @param txHexList
     * @return
     */
    public static List<String> txModuleValidator(Chain chain, String moduleValidator, String moduleCode, List<String> txHexList) {
        //调用交易模块统一验证器
        HashMap params = new HashMap();
        params.put("chianId", chain.getChainId());
        params.put("txHexList", txHexList);
        HashMap result = (HashMap)TransactionCall.request(moduleValidator, moduleCode, params);
        return (List<String>) result.get("list");
    }

}
