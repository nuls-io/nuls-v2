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
public class SignMessageHandler implements Runnable{
    private Chain chain;

    public SignMessageHandler(Chain chain){
        this.chain = chain;
    }

    @Override
    public void run() {
        while(chain.getSignMessageQueue() != null){
            try {
                if(!chain.getSignMessageQueue().isEmpty()){
                    UntreatedMessage untreatedMessage = chain.getHashMessageQueue().take();
                    MessageUtil.handleNewHash(chain, untreatedMessage.getCacheHash(), untreatedMessage.getChainId(), untreatedMessage.getNodeId());
                }
            }catch (Exception e){
                chain.getLogger().error(e);
            }
        }
    }
}
