package io.nuls.crosschain.nuls.utils.thread.handler;

import io.nuls.base.data.NulsDigestData;
import io.nuls.core.exception.NulsException;
import io.nuls.crosschain.base.message.NewOtherCtxMessage;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.nuls.utils.MessageUtil;

public class OtherCtxMessageHandler implements Runnable{
    private Chain chain;

    public OtherCtxMessageHandler(Chain chain){
        this.chain = chain;
    }

    @Override
    public void run() {
        while(chain.getOtherCtxMessageQueue() != null){
            try {
                if(!chain.getOtherCtxMessageQueue().isEmpty()){
                    UntreatedMessage untreatedMessage = chain.getOtherCtxMessageQueue().take();
                    NewOtherCtxMessage messageBody = (NewOtherCtxMessage)untreatedMessage.getMessage();
                    NulsDigestData originalHash = new NulsDigestData();
                    NulsDigestData nativeHash = messageBody.getCtx().getHash();
                    String originalHex;
                    String nativeHex = nativeHash.getDigestHex();
                    //如果是主网接收友链发送过来的跨链交易，则originalHash为跨链交易中txData数据，如果为友链接收主网发送的跨链交易originalHash与Hash一样都是主网协议跨链交易
                    try {
                        if(!chain.isMainChain()){
                            originalHash = messageBody.getRequestHash();
                            originalHex = nativeHex;
                        }else{
                            originalHash.parse(messageBody.getCtx().getTxData(),0);
                            originalHex = originalHash.getDigestHex();
                        }
                    }catch (NulsException e){
                        chain.getMessageLog().error(e);
                        return;
                    }
                    int chainId = untreatedMessage.getChainId();
                    boolean handleResult = MessageUtil.handleNewCtx(messageBody.getCtx(), originalHash, nativeHash, chain, chainId,nativeHex,originalHex,false);
                    NulsDigestData cacheHash = untreatedMessage.getCacheHash();
                    if(!handleResult && chain.getHashNodeIdMap().get(cacheHash)!= null && !chain.getHashNodeIdMap().get(cacheHash).isEmpty()){
                        MessageUtil.regainCtx(chain, chainId, cacheHash, nativeHash, originalHash, originalHex, nativeHex);
                    }
                    chain.getMessageLog().info("新交易处理完成,originalHash:{},Hash:{}\n\n",originalHex,nativeHex);
                }
            }catch (Exception e){
                chain.getMessageLog().error(e);
            }
        }
    }
}
