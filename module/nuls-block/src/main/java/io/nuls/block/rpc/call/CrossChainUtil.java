package io.nuls.block.rpc.call;

import io.nuls.base.protocol.ModuleHelper;
import io.nuls.block.manager.ContextManager;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.util.HashMap;
import java.util.Map;

public class CrossChainUtil {

    /**
     * 批量保存交易
     *
     * @param chainId 链Id/chain id
     * @param height
     * @return
     */
    public static void heightNotice(int chainId, long height) {
        if (!ModuleHelper.isSupportCrossChain()) {
            return;
        }
        NulsLogger commonLog = ContextManager.getContext(chainId).getLogger();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("height", height);
            ResponseMessageProcessor.requestAndResponse(ModuleE.CC.abbr, "newBlockHeight", params);
        } catch (Exception e) {
            commonLog.error("", e);
        }
    }

}
