package io.nuls.crosschain.rpc.call;

import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.model.bo.Chain;

import java.util.HashMap;
import java.util.Map;

/**
 * Calling other modules and transaction related interfaces
 *
 * @author tag
 * @date 2019/4/12
 */
public class BlockCall {
    /**
     * Query block status
     * */
    public static int getBlockStatus(Chain chain) {
        try {
            Map<String, Object> params = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_8);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, "getStatus", params);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("get block status error!");
            }
            return  (int)((HashMap) ((HashMap) cmdResp.getResponseData()).get("getStatus")).get("status");
        } catch (Exception e) {
            chain.getLogger().error(e);
            return 0;
        }
    }

}
