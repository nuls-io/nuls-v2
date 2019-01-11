package io.nuls.transaction.rpc.call;

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.transaction.constant.TxErrorCode;
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
    public static Object request(String cmd, String moduleCode, Map params) throws NulsException {
        try {
            params.put(Constants.VERSION_KEY_STR, "1.0");
            Response cmdResp = CmdDispatcher.requestAndResponse(moduleCode, cmd, params);
            if (!cmdResp.isSuccess()) {
                Log.error("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", moduleCode, cmd, cmdResp.getResponseComment());
                throw new NulsException(TxErrorCode.CALLING_REMOTE_INTERFACE_FAILED);
            }
            return ((HashMap) cmdResp.getResponseData()).get(cmd);
        } catch (Exception e) {
            Log.error("Calling remote interface failed. module:{} - interface:{}", moduleCode, cmd);
            throw new NulsException(e);
        }
    }

    /**
     * txProcess
     * Single transaction txProcess
     * @param chain
     * @param cmd
     * @param moduleCode
     * @param txHex
     * @return
     */
    public static boolean txProcess(Chain chain, String cmd, String moduleCode, String txHex) throws NulsException {
        //调用单个交易验证器
        HashMap params = new HashMap();
        params.put("chianId", chain.getChainId());
        params.put("txHex", txHex);
        HashMap result = (HashMap)TransactionCall.request(cmd, moduleCode, params);
        return (Boolean) result.get("value");
    }

    /**
     * 批量调用模块交易统一验证器
     * Batch call module transaction integrate validator
     * @param chain
     * @param map
     * @return
     */
    public static boolean txsModuleValidators(Chain chain, Map<TxRegister, List<String>> map) throws NulsException {
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
     * 单个模块交易统一验证器
     * Single module transaction integrate validator
     * @param moduleValidator
     * @param txHexList
     * @return 返回未通过验证的交易hash / return unverified transaction hash
     */
    public static List<String> txModuleValidator(Chain chain, String moduleValidator, String moduleCode, List<String> txHexList) throws NulsException {
        //调用交易模块统一验证器
        HashMap params = new HashMap();
        params.put("chianId", chain.getChainId());
        params.put("txHexList", txHexList);
        HashMap result = (HashMap)TransactionCall.request(moduleValidator, moduleCode, params);
        return (List<String>) result.get("list");
    }

}
