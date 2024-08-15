package io.nuls.crosschain.utils.thread.handler;

import io.nuls.base.data.NulsHash;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.GetOtherCtxMessage;
import io.nuls.crosschain.base.message.NewOtherCtxMessage;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.model.bo.NodeType;
import io.nuls.crosschain.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.rpc.call.NetWorkCall;
import io.nuls.crosschain.utils.MessageUtil;


/**
 * Complete cross chain transaction message processing threads broadcasted by other chain nodes
 *
 * @author tag
 * 2019/5/14
 */
public class OtherCtxMessageHandler implements Runnable {
    private Chain chain;

    public OtherCtxMessageHandler(Chain chain) {
        this.chain = chain;
    }

    @Override
    public void run() {
        while (chain.getOtherCtxMessageQueue() != null) {
            NulsHash otherHash = null;
            try {
                UntreatedMessage untreatedMessage = chain.getOtherCtxMessageQueue().take();
                NewOtherCtxMessage messageBody = (NewOtherCtxMessage) untreatedMessage.getMessage();
                otherHash = untreatedMessage.getCacheHash();
                String otherHex = otherHash.toHex();
                int fromChainId = untreatedMessage.getChainId();
                chain.getLogger().info("Start processing other chain nodes：{}Cross chain transactions sent,Hash:{}", untreatedMessage.getNodeId(), otherHex);
                boolean handleResult = MessageUtil.handleOtherChainCtx(messageBody.getCtx(), chain, fromChainId);
                if (!handleResult && chain.getOtherHashNodeIdMap().get(otherHash) != null && !chain.getOtherHashNodeIdMap().get(otherHash).isEmpty()) {
                    regainCtx(chain, fromChainId, otherHash, otherHex);
                }
                chain.getLogger().info("New transaction processing completed,Hash:{}\n\n", otherHex);
            } catch (Exception e) {
                chain.getLogger().error(e);
            } finally {
                if (otherHash != null) {
                    chain.getOtherCtxStageMap().remove(otherHash);
                }
            }
        }
    }

    /**
     * From broadcast transactionshashObtain complete cross chain transaction processing from the node of the signed message
     *
     * @param chain     This chain information
     * @param chainId   Sending ChainID
     * @param cacheHash Cached transactionsHash
     */
    private void regainCtx(Chain chain, int chainId, NulsHash cacheHash, String nativeHex) {
        NodeType nodeType = chain.getOtherHashNodeIdMap().get(cacheHash).remove(0);
        if (chain.getOtherHashNodeIdMap().get(cacheHash).isEmpty()) {
            chain.getOtherHashNodeIdMap().remove(cacheHash);
        }
        GetOtherCtxMessage responseMessage = new GetOtherCtxMessage();
        responseMessage.setRequestHash(cacheHash);
        NetWorkCall.sendToNode(chainId, responseMessage, nodeType.getNodeId(), CommandConstant.GET_OTHER_CTX_MESSAGE);
        chain.getLogger().info("Cross chain transaction processing failed, sending to other chain nodes：{}Retrieve cross chain transactions again,Hash:{}", nodeType.getNodeId(), nativeHex);

    }
}
