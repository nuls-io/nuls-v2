package io.nuls.account.rpc.call;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.RpcConstant;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.core.ioc.ScanUtil;
import io.nuls.tools.log.Log;
import io.nuls.tools.protocol.ResisterTx;
import io.nuls.tools.protocol.TxMethodType;
import io.nuls.tools.protocol.TxRegisterDetail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: qinyifeng
 * @description: 交易模块接口调用
 * @date: 2018/11/27
 */
public class TransactionCmdCall {

    /**
     * 向交易模块注册交易
     * Register transactions with the transaction module
     */
    public static boolean registerTx(int chainId) {
        try {
            List<Class> classList = ScanUtil.scan(AccountConstant.RPC_PATH);
            if (classList == null || classList.size() == 0) {
                return false;
            }
            List<TxRegisterDetail> txRegisterDetailList = new ArrayList<>();
            Map<Integer, TxRegisterDetail> registerDetailMap = new HashMap<>(16);
            for (Class clz : classList) {
                Method[] methods = clz.getDeclaredMethods();
                for (Method method : methods) {
                    ResisterTx annotation = getRegisterAnnotation(method);
                    if (annotation != null) {
                        if (!registerDetailMap.containsKey(annotation.txType().txType)) {
                            TxRegisterDetail txRegisterDetail = new TxRegisterDetail(annotation.txType());
                            registerDetailMap.put(annotation.txType().txType, txRegisterDetail);
                            txRegisterDetailList.add(txRegisterDetail);
                        }
                        if (annotation.methodType().equals(TxMethodType.COMMIT)) {
                            registerDetailMap.get(annotation.txType().txType).setCommit(annotation.methodName());
                        } else if (annotation.methodType().equals(TxMethodType.VALID)) {
                            registerDetailMap.get(annotation.txType().txType).setValidator(annotation.methodName());
                        } else if (annotation.methodType().equals(TxMethodType.ROLLBACK)) {
                            registerDetailMap.get(annotation.txType().txType).setRollback(annotation.methodName());
                        }
                    }
                }
            }
            //向交易管理模块注册交易
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, RpcConstant.TX_REGISTER_VERSION);
            params.put(RpcConstant.TX_CHAIN_ID, chainId);
            params.put(RpcConstant.TX_MODULE_CODE, ModuleE.AC.abbr);
            params.put(RpcConstant.TX_MODULE_VALIDATE_CMD, "ac_accountTxValidate");
            params.put(RpcConstant.TX_MODULE_COMMIT_CMD, "ac_commitTx");
            params.put(RpcConstant.TX_MODULE_ROLLBACK_CMD, "ac_rollbackTx");
            params.put("list", txRegisterDetailList);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, RpcConstant.TX_REGISTER_CMD, params);
            if (!cmdResp.isSuccess()) {
                Log.error("chain ：" + chainId + " Failure of transaction registration,errorMsg: " + cmdResp.getResponseComment());
                return false;
            }
        } catch (Exception e) {
            Log.error("", e);
        }
        return true;
    }

    /**
     * 扫描需要注册到交易模块的交易
     *
     * @param method
     * @return
     */
    private static ResisterTx getRegisterAnnotation(Method method) {
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (ResisterTx.class.equals(annotation.annotationType())) {
                return (ResisterTx) annotation;
            }
        }
        return null;
    }

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
