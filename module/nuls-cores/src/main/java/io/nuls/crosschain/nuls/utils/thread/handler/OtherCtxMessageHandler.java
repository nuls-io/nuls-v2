package io.nuls.crosschain.nuls.utils.thread.handler;

import io.nuls.base.data.NulsHash;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.GetOtherCtxMessage;
import io.nuls.crosschain.base.message.NewOtherCtxMessage;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.bo.NodeType;
import io.nuls.crosschain.nuls.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.utils.MessageUtil;


/**
 * 其他链节点广播过来的完整跨链交易消息处理线程
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
                chain.getLogger().debug("开始处理其他链节点：{}发送的跨链交易,Hash:{}", untreatedMessage.getNodeId(), otherHex);
                boolean handleResult = MessageUtil.handleOtherChainCtx(messageBody.getCtx(), chain, fromChainId);
                if (!handleResult && chain.getOtherHashNodeIdMap().get(otherHash) != null && !chain.getOtherHashNodeIdMap().get(otherHash).isEmpty()) {
                    regainCtx(chain, fromChainId, otherHash, otherHex);
                }
                chain.getLogger().debug("新交易处理完成,Hash:{}\n\n", otherHex);
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
     * 从广播交易hash或签名消息的节点中获取完整跨链交易处理
     *
     * @param chain     本链信息
     * @param chainId   发送链ID
     * @param cacheHash 缓存的交易Hash
     */
    private void regainCtx(Chain chain, int chainId, NulsHash cacheHash, String nativeHex) {
        NodeType nodeType = chain.getOtherHashNodeIdMap().get(cacheHash).remove(0);
        if (chain.getOtherHashNodeIdMap().get(cacheHash).isEmpty()) {
            chain.getOtherHashNodeIdMap().remove(cacheHash);
        }
        GetOtherCtxMessage responseMessage = new GetOtherCtxMessage();
        responseMessage.setRequestHash(cacheHash);
        NetWorkCall.sendToNode(chainId, responseMessage, nodeType.getNodeId(), CommandConstant.GET_OTHER_CTX_MESSAGE);
        chain.getLogger().info("跨链交易处理失败，向其他链节点：{}重新获取跨链交易，Hash:{}", nodeType.getNodeId(), nativeHex);

    }
}
