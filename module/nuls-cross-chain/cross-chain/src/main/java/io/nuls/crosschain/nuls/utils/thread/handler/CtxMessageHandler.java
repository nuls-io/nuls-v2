package io.nuls.crosschain.nuls.utils.thread.handler;

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
            try {
                if (!chain.getCtxMessageQueue().isEmpty()) {
                    UntreatedMessage untreatedMessage = chain.getCtxMessageQueue().take();
                    NewCtxMessage messageBody = (NewCtxMessage) untreatedMessage.getMessage();
                    byte[] originalHash = messageBody.getCtx().getTxData();
                    byte[] nativeHash = messageBody.getRequestHash();
                    String nativeHex = HashUtil.toHex(nativeHash);
                    String originalHex = HashUtil.toHex(originalHash);
                    int chainId = untreatedMessage.getChainId();
                    boolean handleResult = MessageUtil.handleNewCtx(messageBody.getCtx(), originalHash, nativeHash, chain, chainId, nativeHex, originalHex, true);
                    byte[] cacheHash = untreatedMessage.getCacheHash();
                    if (!handleResult && chain.getHashNodeIdMap().get(cacheHash) != null && !chain.getHashNodeIdMap().get(cacheHash).isEmpty()) {
                        MessageUtil.regainCtx(chain, chainId, cacheHash, nativeHash, originalHash, originalHex, nativeHex);
                    }
                    chain.getLogger().info("新交易处理完成,originalHash:{},Hash:{}\n\n", originalHex, nativeHex);
                }
            } catch (Exception e) {
                chain.getLogger().error(e);
            }
        }
    }
}
