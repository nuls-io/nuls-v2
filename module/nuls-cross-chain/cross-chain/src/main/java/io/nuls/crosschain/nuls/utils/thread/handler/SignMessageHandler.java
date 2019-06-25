package io.nuls.crosschain.nuls.utils.thread.handler;

import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.nuls.utils.MessageUtil;

/**
 * 链内节点广播过来的跨链交易签名处理线程
 *
 * @author tag
 * 2019/5/14
 */
public class SignMessageHandler implements Runnable {
    private Chain chain;

    public SignMessageHandler(Chain chain) {
        this.chain = chain;
    }

    @Override
    public void run() {
        while (chain.getSignMessageQueue() != null) {
            try {
                UntreatedMessage untreatedMessage = chain.getSignMessageQueue().take();
                String nativeHex = untreatedMessage.getCacheHash().toHex();
                chain.getLogger().info("开始处理链内节点{}广播过来的跨链交易签名消息,Hash：{}", untreatedMessage.getNodeId(), nativeHex);
                MessageUtil.handleSignMessage(chain, untreatedMessage.getCacheHash(), untreatedMessage.getChainId(), untreatedMessage.getNodeId(), nativeHex);
            } catch (Exception e) {
                chain.getLogger().error(e);
            }
        }
    }
}
