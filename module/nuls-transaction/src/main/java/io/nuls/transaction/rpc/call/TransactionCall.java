package io.nuls.transaction.rpc.call;

import io.nuls.base.data.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.utils.TxUtil;

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
     * 调用各交易验证器
     * @param chain
     * @param txRegister 交易注册信息
     * @param tx
     * @return
     * @throws NulsException
     */
    public static boolean txValidatorProcess(Chain chain, TxRegister txRegister, String tx) throws NulsException {
        try {
            if(StringUtils.isBlank(txRegister.getValidator())){
                //交易没有注册验证器cmd的交易,包括系统交易,则直接返回true
                return true;
            }
            //调用单个交易验证器
            Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("tx", tx);
            Map result = (Map) TransactionCall.requestAndResponse(txRegister.getModuleCode(), txRegister.getValidator(), params);
            Boolean value = (Boolean) result.get("value");
            if (null == value) {
                chain.getLogger().error("call module-{} validator {} response value is null, error:{}",
                        txRegister.getModuleCode(), txRegister.getValidator(), TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
                return false;
            }
            return value;
        } catch (RuntimeException e) {
            chain.getLogger().error(e);
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
        } catch (NulsException e){
            chain.getLogger().error(e);
            return false;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
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
     * @return 返回未通过验证的交易hash, 如果出现异常那么交易全部返回(不通过) / return unverified transaction hash
     */
    public static List<String> txModuleValidator(Chain chain, String moduleValidator, String moduleCode, List<String> txList) throws NulsException {
        return txModuleValidator(chain, moduleValidator, moduleCode, txList, null);
    }

    /**
     * 单个模块交易统一验证器
     * Single module transaction integrate validator
     *
     * @return 返回未通过验证的交易hash, 如果出现异常那么交易全部返回(不通过) / return unverified transaction hash
     */
    public static List<String> txModuleValidator(Chain chain, String moduleValidator, String moduleCode, List<String> txList, String blockHeaderStr) throws NulsException {
        try {
            //调用交易模块统一验证器
            Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("txList", txList);
            params.put("blockHeader", blockHeaderStr);
            Map result = (Map) TransactionCall.requestAndResponse(moduleCode, moduleValidator, params);

            List<String> list = (List<String>) result.get("list");
            if (null == list) {
                chain.getLogger().error("call module-{} {} response value is null, error:{}",
                        moduleCode, moduleValidator, TxErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
                return new ArrayList<>(txList.size());
            }
            return list;
        } catch (Exception e) {
            chain.getLogger().error(e);
            List<String> hashList = new ArrayList<>(txList.size());
            for(String txStr : txList){
                Transaction tx = TxUtil.getInstanceRpcStr(txStr, Transaction.class);
                hashList.add(tx.getHash().toHex());
            }
            return hashList;
        }
    }

}
