package io.nuls.crosschain.nuls.utils.thread.handler;

import io.nuls.base.data.NulsHash;
import io.nuls.crosschain.base.message.NewCtxMessage;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.nuls.utils.MessageUtil;

/**
 * 链内节点广播的完整跨链交易处理线程
 *
 * @author tag
 * 2019/5/14
 */
public class CtxMessageHandler implements Runnable {
    private Chain chain;

    public CtxMessageHandler(Chain chain) {
        this.chain = chain;
    }

    @Override
    public void run() {
        while (chain.getCtxMessageQueue() != null) {
            NulsHash cacheHash = null;
            try {
                UntreatedMessage untreatedMessage = chain.getCtxMessageQueue().take();
                NewCtxMessage messageBody = (NewCtxMessage) untreatedMessage.getMessage();
                NulsHash nativeHash = messageBody.getCtx().getHash();
                String nativeHex = nativeHash.toHex();
                int chainId = untreatedMessage.getChainId();
                chain.getLogger().info("开始处理链内节点：{}发送的跨链交易,Hash:{}", untreatedMessage.getNodeId(), nativeHex);
                boolean handleResult = MessageUtil.handleInChainCtx(messageBody.getCtx(), chain);
                cacheHash = untreatedMessage.getCacheHash();
                if (!handleResult && chain.getHashNodeIdMap().get(nativeHash) != null && !chain.getHashNodeIdMap().get(nativeHash).isEmpty()) {
                    MessageUtil.regainCtx(chain, chainId, cacheHash,nativeHex,true);
                }
                chain.getLogger().info("新交易处理完成,Hash:{}\n\n", nativeHex);
            } catch (Exception e) {
                chain.getLogger().error(e);
            } finally {
                if (cacheHash != null) {
                    chain.getCtxStageMap().remove(cacheHash);
                }
            }
        }
    }
}
