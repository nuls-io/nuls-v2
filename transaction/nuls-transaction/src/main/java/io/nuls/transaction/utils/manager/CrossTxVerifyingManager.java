package io.nuls.transaction.utils.manager;

import io.nuls.base.data.NulsDigestData;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.CrossChainTx;

import java.util.Map;

/**
 * 管理接收的其他链创建的跨链交易, 暂存验证中的跨链交易.
 * @author: Charlie
 * @date: 2018/11/26
 */
public class CrossTxVerifyingManager {

    //private static final CrossTxVerifyingManager INSTANCE = new CrossTxVerifyingManager();

    //private Map<NulsDigestData, CrossChainTx> crossTxVerifyingMap;

    public CrossTxVerifyingManager(Chain chain){
        //TODO 查数据库
        //this.crossTxVerifyingMap = new HashMap<>();

    }

//    public static CrossTxVerifyingManager getInstance(){
//        return INSTANCE;
//    }

    public void putCrossChainTx(Chain chain, CrossChainTx crossChainTx){
        chain.getCrossTxVerifyingMap().put(crossChainTx.getTx().getHash(), crossChainTx);
    }

    public boolean containsKey(Chain chain, NulsDigestData hash){
        return chain.getCrossTxVerifyingMap().containsKey(hash);
    }

    public void removeCrossChainTx(Chain chain, NulsDigestData hash){
        chain.getCrossTxVerifyingMap().remove(hash);
    }

    public Map<NulsDigestData, CrossChainTx> getCrossTxVerifyingMap(Chain chain){
        return chain.getCrossTxVerifyingMap();
    }

    public boolean updateCrossChainTxState(Chain chain, NulsDigestData hash, int state){
        boolean rs = false;
        if(chain.getCrossTxVerifyingMap().containsKey(hash)){
            CrossChainTx crossChainTx = chain.getCrossTxVerifyingMap().get(hash);
            crossChainTx.setState(state);
            rs = true;
        }
        return rs;
    }
}
