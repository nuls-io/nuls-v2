package io.nuls.crosschain.base.rpc.call;

import io.nuls.core.rpc.info.Constants;
import io.nuls.crosschain.base.model.dto.ModuleTxRegisterDTO;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.log.Log;

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
            Log.error(e);
            return false;
        }
    }
}
