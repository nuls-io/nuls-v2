package io.nuls.transaction.rpc.call;

import io.nuls.core.constant.BaseConstant;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.bo.Chain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: qinyifeng
 * @date: 2018/12/05
 */
public class TransactionCall {


    public static Object requestAndResponse(String moduleCode, String cmd, Map params) throws NulsException {
        return requestAndResponse(moduleCode, cmd, params, null);
    }
    /**
     * 调用其他模块接口
     * Call other module interfaces
     */
    public static Object requestAndResponse(String moduleCode, String cmd, Map params, Long timeout) throws NulsException {
        try {
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            Response response = null;
            try {
                if(null == timeout) {
                    response = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params);
                }else{
                    response = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params, timeout);
                }
            } catch (Exception e) {
                LOG.error(e);
                throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
            }
            if (!response.isSuccess()) {
                String errorCode = response.getResponseErrorCode();
                LOG.error("Call interface [{}] error, ErrorCode is {}, ResponseComment:{}", cmd, errorCode, response.getResponseComment());
                throw new NulsException(ErrorCode.init(errorCode));
            }
            Map data = (Map)response.getResponseData();
            return data.get(cmd);
        } catch (RuntimeException e) {
            LOG.error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    /**
     * 调用交易的 commit 或者 rollback
     * @param chain
     * @param cmd
     * @param moduleCode
     * @param txList
     * @return
     */
    public static boolean txProcess(Chain chain, String cmd, String moduleCode,  List<String> txList, String blockHeader) {
        try {
            //调用单个交易验证器
            Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("txList", txList);
            params.put("blockHeader", blockHeader);
            Map result = (Map) TransactionCall.requestAndResponse(moduleCode, cmd, params);
            Boolean value = (Boolean) result.get("value");
            if (null == value) {
            chain.getLogger().error("call module-{} {} response value is null, error:{}",
                    moduleCode, cmd, TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
            return false;
                }
            return value;
        } catch (Exception e) {
            chain.getLogger().error("call module-{} {} error, error:{}", moduleCode, "txProcess", e);
            return false;
        }
    }

    /**
     * 模块交易统一验证器
     * Single module transaction integrate validator
     *
     * @return 返回未通过验证的交易hash, 如果出现异常那么交易全部返回(不通过) / return unverified transaction hash
     */
    public static Map<String, Object> txModuleValidator(Chain chain, String moduleCode, String tx) throws NulsException {
        List<String> txList = new ArrayList<>();
        txList.add(tx);
        return callTxModuleValidator(chain, moduleCode, txList, null);
    }

    /**
     * 模块交易统一验证器
     * Single module transaction integrate validator
     *
     * @return 返回未通过验证的交易hash, 如果出现异常那么交易全部返回(不通过) / return unverified transaction hash
     */
    public static List<String> txModuleValidator(Chain chain, String moduleCode, List<String> txList) throws NulsException {
        return txModuleValidator(chain, moduleCode, txList, null);
    }

    /**
     * 模块交易统一验证器
     * Single module transaction integrate validator
     *
     * @return 返回未通过验证的交易hash, 如果出现异常那么交易全部返回(不通过) / return unverified transaction hash
     */
    public static List<String> txModuleValidator(Chain chain, String moduleCode, List<String> txList, String blockHeaderStr) throws NulsException {
        //调用交易模块统一验证器
        Map<String, Object> result = callTxModuleValidator(chain, moduleCode, txList, blockHeaderStr);
        return (List<String>) result.get("list");
    }

    private static Map<String, Object> callTxModuleValidator(Chain chain, String moduleCode, List<String> txList, String blockHeaderStr) throws NulsException {
        Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
        params.put(Constants.CHAIN_ID, chain.getChainId());
        params.put("txList", txList);
        params.put("blockHeader", blockHeaderStr);
        Map responseMap = (Map) TransactionCall.requestAndResponse(moduleCode, BaseConstant.TX_VALIDATOR, params);

        List<String> list = (List<String>) responseMap.get("list");
        if (null == list) {
            chain.getLogger().error("call module-{} {} response value is null, error:{}",
                    moduleCode, BaseConstant.TX_VALIDATOR, TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
            list = new ArrayList<>(txList.size());
        }

        String errorCode = (String) responseMap.get("errorCode");
        Map<String, Object> result = new HashMap<>(TxConstant.INIT_CAPACITY_4);
        result.put("list", list);
        result.put("errorCode", errorCode);
        return result;
    }

}
