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
            NulsHash cacheHash = null;
            try {
                UntreatedMessage untreatedMessage = chain.getOtherCtxMessageQueue().take();
                NewOtherCtxMessage messageBody = (NewOtherCtxMessage) untreatedMessage.getMessage();
                NulsHash originalHash;
                String originalHex;
                NulsHash nativeHash = messageBody.getCtx().getHash();
                String nativeHex = nativeHash.toHex();
                //如果是主网接收友链发送过来的跨链交易，则originalHash为跨链交易中txData数据，如果为友链接收主网发送的跨链交易originalHash与Hash一样都是主网协议跨链交易
                if (!chain.isMainChain()) {
                    originalHash = messageBody.getRequestHash();
                    originalHex = nativeHex;
                } else {
                    originalHash = new NulsHash(messageBody.getCtx().getTxData());
                    originalHex = originalHash.toHex();
                }

                int chainId = untreatedMessage.getChainId();
                chain.getLogger().info("开始处理其他链节点：{}发送的跨链交易,originalHash:{},Hash:{}", untreatedMessage.getNodeId(), originalHex, nativeHex);
                boolean handleResult = MessageUtil.handleNewCtx(messageBody.getCtx(), originalHash, nativeHash, chain, chainId, nativeHex, originalHex, false);
                cacheHash = untreatedMessage.getCacheHash();
                if (!handleResult && chain.getHashNodeIdMap().get(cacheHash) != null && !chain.getHashNodeIdMap().get(cacheHash).isEmpty()) {
                    MessageUtil.regainCtx(chain, chainId, cacheHash, nativeHash, originalHash, originalHex, nativeHex);
                }
                chain.getLogger().info("新交易处理完成,originalHash:{},Hash:{}\n\n", originalHex, nativeHex);
            } catch (Exception e) {
                chain.getLogger().error(e);
            }finally {
                if(cacheHash != null){
                    chain.getCtxStageMap().remove(cacheHash);
                }
            }
        }
    }
}
