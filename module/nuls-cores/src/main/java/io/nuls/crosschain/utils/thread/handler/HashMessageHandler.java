package io.nuls.crosschain.utils.thread.handler;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.utils.MessageUtil;

/**
 * Cross chain transactions broadcasted by other chain nodesHashMessage processing thread
 *
 * @author tag
 * 2019/5/14
 */
public class HashMessageHandler implements Runnable{
    private Chain chain;

    public HashMessageHandler(Chain chain){
        this.chain = chain;
    }

    @Override
    public void run() {
        while(chain.getHashMessageQueue() != null){
            try {
                UntreatedMessage untreatedMessage = chain.getHashMessageQueue().take();
                String nativeHex = untreatedMessage.getCacheHash().toHex();
                chain.getLogger().info("Start processing other chain nodes{}Cross chain transactions broadcasted overHashnews,Hash：{}", untreatedMessage.getNodeId(), nativeHex);
                MessageUtil.handleNewHashMessage(chain, untreatedMessage.getCacheHash(), untreatedMessage.getChainId(), untreatedMessage.getNodeId(),nativeHex);
            }catch (Exception e){
                chain.getLogger().error(e);
            }
        }
    }
}
