package io.nuls.crosschain.nuls.utils.thread.handler;

import io.nuls.base.data.NulsHash;
import io.nuls.crosschain.base.message.NewOtherCtxMessage;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.bo.message.UntreatedMessage;
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
                chain.getLogger().info("开始处理其他链节点：{}发送的跨链交易,Hash:{}", untreatedMessage.getNodeId(), otherHex);
                boolean handleResult = MessageUtil.handleOtherChainCtx(messageBody.getCtx(),chain, fromChainId);
                if (!handleResult && chain.getOtherHashNodeIdMap().get(otherHash) != null && !chain.getOtherHashNodeIdMap().get(otherHash).isEmpty()) {
                    MessageUtil.regainCtx(chain, fromChainId, otherHash, otherHex,false);
                }
                chain.getLogger().info("新交易处理完成,Hash:{}\n\n", otherHex);
            } catch (Exception e) {
                chain.getLogger().error(e);
            } finally {
                if (otherHash != null) {
                    chain.getOtherCtxStageMap().remove(otherHash);
                }
            }
        }
    }
}
