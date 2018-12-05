package io.nuls.transaction.utils;

import io.nuls.base.data.NulsDigestData;
import io.nuls.transaction.model.bo.CrossChainTx;

import java.util.Map;

/**
 * 管理接收的其他链创建的跨链交易, 暂存验证中的跨链交易.
 * @author: Charlie
 * @date: 2018/11/26
 */
public class CrossTxVerifyingManager {

    private static final CrossTxVerifyingManager INSTANCE = new CrossTxVerifyingManager();

    private Map<NulsDigestData, CrossChainTx> crossTxVerifyingMap;

    private CrossTxVerifyingManager(){
        //TODO 查数据库
        //this.crossTxVerifyingMap = new HashMap<>();

    }

    public static CrossTxVerifyingManager getInstance(){
        return INSTANCE;
    }

    public void putCrossChainTx(CrossChainTx crossChainTx){
        this.crossTxVerifyingMap.put(crossChainTx.getTx().getHash(), crossChainTx);
    }

    public boolean containsKey(NulsDigestData hash){
        return crossTxVerifyingMap.containsKey(hash);
    }

    public void removeCrossChainTx(NulsDigestData hash){
        this.crossTxVerifyingMap.remove(hash);
    }

    public Map<NulsDigestData, CrossChainTx> getCrossTxVerifyingMap(){
        return this.crossTxVerifyingMap;
    }

    public boolean updateCrossChainTxState(NulsDigestData hash, int state){
        boolean rs = false;
        if(this.crossTxVerifyingMap.containsKey(hash)){
            CrossChainTx crossChainTx = this.crossTxVerifyingMap.get(hash);
            crossChainTx.setState(state);
            rs = true;
        }
        return rs;
    }
}
