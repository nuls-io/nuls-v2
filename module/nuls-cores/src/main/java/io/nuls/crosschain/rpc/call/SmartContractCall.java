package io.nuls.crosschain.rpc.call;

import io.nuls.core.log.Log;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.crosschain.model.bo.CmdRegisterDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calling other modules and transaction related interfaces
 *
 * @author tag
 * 2018/12/26
 */
public class SmartContractCall {
    /**
     * Registering smart contract transactions
     * Acquire account lock-in amount and available balance
     *
     * @param chainId
     */
    @SuppressWarnings("unchecked")
    public static boolean registerContractTx(int chainId, List<CmdRegisterDto> cmdRegisterDtoList) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("moduleCode", ModuleE.CC.abbr);
        params.put("cmdRegisterList", cmdRegisterDtoList);
        try {
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, "sc_register_cmd_for_contract", params);
            return callResp.isSuccess();
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }
}
