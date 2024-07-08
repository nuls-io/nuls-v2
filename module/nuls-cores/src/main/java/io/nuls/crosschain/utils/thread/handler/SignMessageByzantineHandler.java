package io.nuls.crosschain.utils.thread.handler;

import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.base.utils.HashSetTimeDuplicateProcessor;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.utils.MessageUtil;

/**
 * Byzantine verification processing thread for cross chain transaction signatures broadcasted by intra chain nodes
 *
 * @author tag
 * 2019/8/8
 */

public class SignMessageByzantineHandler implements Runnable {
    private Chain chain;

    private HashSetTimeDuplicateProcessor processor = new HashSetTimeDuplicateProcessor(1000, 300000L);

    public SignMessageByzantineHandler(Chain chain) {
        this.chain = chain;
    }

    @Override
    public void run() {
        while (chain.getSignMessageByzantineQueue() != null) {
            try {
                UntreatedMessage untreatedMessage = chain.getSignMessageByzantineQueue().take();
//                if (!processor.insertAndCheck(untreatedMessage.getNodeId() + untreatedMessage.getCacheHash().toHex())) {
//                    chain.getLogger().info("D discard ： " + untreatedMessage.getNodeId() + "," + untreatedMessage.getCacheHash().toHex());
//                    continue;
//                }
                chain.getLogger().info("D process ： " + untreatedMessage.getNodeId() + "," + untreatedMessage.getCacheHash().toHex());
                String nativeHex = untreatedMessage.getCacheHash().toHex();
                chain.getLogger().debug("Start monitoring nodes within the chain{}Cross chain transaction signature message broadcasted for signature Byzantine verification,Hash：{}", untreatedMessage.getNodeId(), nativeHex);
                MessageUtil.handleSignMessage(chain, untreatedMessage.getCacheHash(), untreatedMessage.getChainId(), untreatedMessage.getNodeId(), (BroadCtxSignMessage) untreatedMessage.getMessage(), nativeHex);
            } catch (Exception e) {
                chain.getLogger().error(e);
            }
        }
    }
}
