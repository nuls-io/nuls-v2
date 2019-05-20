package io.nuls.crosschain.nuls.utils.handler;

import io.nuls.base.data.NulsDigestData;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;

import java.util.Iterator;
import java.util.Map;

/**
 * 跨链交易下载线程
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
        if(chain.getCtxStageMap() != null){
            Iterator<Map.Entry<NulsDigestData, Integer>> it = chain.getCtxStageMap().entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<NulsDigestData, Integer> entry = it.next();
                if(entry.getValue() == NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE){
                    int tryCount = 0;

                }else{
                    it.remove();
                }
            }
        }
    }
}
