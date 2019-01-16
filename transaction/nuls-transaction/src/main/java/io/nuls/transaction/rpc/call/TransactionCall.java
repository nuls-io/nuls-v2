package io.nuls.transaction.rpc.call;

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.transaction.constant.TxConstant;
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
    public static Object request(String moduleCode, String cmd, Map params) throws NulsException {
        try {
            params.put(Constants.VERSION_KEY_STR, "1.0");
            Response cmdResp = CmdDispatcher.requestAndResponse(moduleCode, cmd, params);
            if (!cmdResp.isSuccess()) {
                Log.error("Calling remote interface failed. module:{} - interface:{} - responseStatus:{} - ResponseComment:{}",
                        moduleCode, cmd, cmdResp.getResponseStatus(), cmdResp.getResponseComment());
                throw new NulsException(TxErrorCode.CALLING_REMOTE_INTERFACE_FAILED);
            }
            Map resData = (Map)cmdResp.getResponseData();
            if (null == resData) {
                return null;
            }
            return resData.get(cmd);
        } catch (Exception e) {
            Log.error("Calling remote interface failed. module:{} - interface:{}", moduleCode, cmd);
            throw new NulsException(e);
        }
    }

    /**
     * txProcess 根据交易模块code向各模块提交交易
     * Single transaction txProcess
     *
     * @param chain
     * @param cmd
     * @param moduleCode
     * @param txHex
     * @return
     */
    public static boolean txProcess(Chain chain, String cmd, String moduleCode, String txHex) throws NulsException {
        //调用单个交易验证器
        Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
        params.put("chainId", chain.getChainId());
        params.put("txHex", txHex);
        Map result = (Map) TransactionCall.request(moduleCode, cmd, params);
        return (Boolean) result.get("value");
    }

    /**
     * 批量调用模块交易统一验证器
     * Batch call module transaction integrate validator
     *
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
     *
     * @param moduleValidator
     * @param txHexList
     * @return 返回未通过验证的交易hash / return unverified transaction hash
     */
    public static List<String> txModuleValidator(Chain chain, String moduleValidator, String moduleCode, List<String> txHexList) throws NulsException {
        //调用交易模块统一验证器
        Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
        params.put("chainId", chain.getChainId());
        params.put("txHexList", txHexList);
        Map result = (Map) TransactionCall.request(moduleCode, moduleValidator, params);
        return (List<String>) result.get("list");
    }

}
