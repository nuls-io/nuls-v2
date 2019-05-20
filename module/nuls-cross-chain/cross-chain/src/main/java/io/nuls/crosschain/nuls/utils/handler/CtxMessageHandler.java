package io.nuls.crosschain.nuls.utils.handler;

import io.nuls.base.data.NulsDigestData;
import io.nuls.crosschain.base.message.NewCtxMessage;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.nuls.utils.MessageUtil;


public class CtxMessageHandler implements Runnable{
    private Chain chain;

    public CtxMessageHandler(Chain chain){
        this.chain = chain;
    }

    @Override
    public void run() {
        while(chain.getCtxMessageQueue() != null){
            try {
                if(!chain.getCtxMessageQueue().isEmpty()){
                    UntreatedMessage untreatedMessage = chain.getCtxMessageQueue().take();
                    NewCtxMessage messageBody = (NewCtxMessage)untreatedMessage.getMessage();
                    NulsDigestData originalHash = new NulsDigestData();
                    originalHash.parse(messageBody.getCtx().getTxData(),0);
                    NulsDigestData nativeHash = messageBody.getRequestHash();
                    String nativeHex = nativeHash.getDigestHex();
                    String originalHex = originalHash.getDigestHex();
                    int chainId = untreatedMessage.getChainId();
                    boolean handleResult = MessageUtil.handleNewCtx(messageBody.getCtx(), originalHash, nativeHash, chain, chainId,nativeHex,originalHex,true);
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
