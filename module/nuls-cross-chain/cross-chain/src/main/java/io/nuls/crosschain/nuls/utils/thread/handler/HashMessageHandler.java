package io.nuls.crosschain.nuls.utils.thread.handler;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.nuls.utils.MessageUtil;

/**
 * 其他链节点广播的跨链交易Hash消息处理线程
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
                if(!chain.getHashMessageQueue().isEmpty()){
                    UntreatedMessage untreatedMessage = chain.getHashMessageQueue().take();
                    String nativeHex = untreatedMessage.getCacheHash().toHex();
                    chain.getLogger().info("开始处理其他链节点{}广播过来的跨链交易Hash消息,Hash：{}", untreatedMessage.getNodeId(), nativeHex);
                    MessageUtil.handleNewHash(chain, untreatedMessage.getCacheHash(), untreatedMessage.getChainId(), untreatedMessage.getNodeId(),nativeHex);
                }
            }catch (Exception e){
                chain.getLogger().error(e);
            }
        }
    }
}
