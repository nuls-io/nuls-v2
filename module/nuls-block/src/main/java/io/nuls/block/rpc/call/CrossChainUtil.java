package io.nuls.block.rpc.call;

import io.nuls.block.manager.ContextManager;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.rpc.util.ModuleHelper;
import io.nuls.tools.log.logback.NulsLogger;

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
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("height", height);
            ResponseMessageProcessor.requestAndResponse(ModuleE.CC.abbr, "newBlockHeight", params);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
        }
    }

}
