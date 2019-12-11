package io.nuls.crosschain.nuls.rpc.call;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.BlockHeader;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.rpc.callback.NewBlockHeightInvoke;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.exception.NulsException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author tag
 * @date 2019/4/12
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

    /**
     * 查询区块状态
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
            return 1;
        }
    }

    /**
     * 查询最新区块高度
     * */
    public static BlockHeader getLatestBlockHeader(Chain chain) {
        try {
            Map<String, Object> params = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_8);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, "latestBlockHeader", params);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("get block status error!");
            }
            Map result = (Map) ((HashMap) cmdResp.getResponseData()).get("latestBlockHeader");
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.parse(RPCUtil.decode((String) result.get("value")),0);
            return blockHeader;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return null;
        }
    }
}
