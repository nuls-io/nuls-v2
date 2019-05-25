package io.nuls.crosschain.nuls.rpc.call;

import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.rpc.callback.NewBlockHeightInvoke;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.exception.NulsException;

import java.util.HashMap;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: tag
 * @date: 2019/4/12
 */
public class BlockCall {
    /**
     * 区块最新高度
     * */
    public static boolean subscriptionNewBlockHeight(Chain chain) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chain.getChainId());
            String messageId = ResponseMessageProcessor.requestAndInvoke(ModuleE.BL.abbr, "latestHeight",
                    params, "0", "1", new NewBlockHeightInvoke());
            if(null != messageId){
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }
}
