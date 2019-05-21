package io.nuls.crosschain.nuls.rpc.call;

import io.nuls.core.rpc.info.Constants;
import io.nuls.crosschain.base.model.dto.ModuleTxRegisterDTO;
import io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.crosschain.nuls.utils.LoggerUtil;

import java.util.HashMap;
import java.util.Map;
/**
 * 与交易模块交互类
 * Interaction class with transaction module
 * @author tag
 * 2019/4/10
 */
public class TransactionCall {
    /**
     * 交易注册
     */
    @SuppressWarnings("unchecked")
    public static boolean registerTx(ModuleTxRegisterDTO moduleTxRegisterDTO) {
        try {
            Map<String, Object> params = new HashMap(4);
            params.put(Constants.CHAIN_ID, moduleTxRegisterDTO.getChainId());
            params.put("list", moduleTxRegisterDTO.getList());
            params.put("moduleCode", moduleTxRegisterDTO.getModuleCode());
            params.put("moduleValidator", moduleTxRegisterDTO.getModuleValidator());
            params.put("moduleCommit", moduleTxRegisterDTO.getModuleCommit());
            params.put("moduleRollback", moduleTxRegisterDTO.getModuleRollback());
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_register", params);
            if (!cmdResp.isSuccess()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return false;
        }
    }

    /**
     * 将新创建的交易发送给交易管理模块
     * The newly created transaction is sent to the transaction management module
     *
     * @param chain chain info
     * @param tx transaction hex
     */
    @SuppressWarnings("unchecked")
    public static boolean sendTx(Chain chain, String tx) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
        params.put("tx", tx);
        try {
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
            if (!cmdResp.isSuccess()) {
                chain.getRpcLogger().error("Transaction failed to send!");
                throw new NulsException(NulsCrossChainErrorCode.FAILED);
            }
            return true;
        }catch (NulsException e){
            throw e;
        }catch (Exception e) {
            chain.getRpcLogger().error(e);
            return false;
        }
    }
}
