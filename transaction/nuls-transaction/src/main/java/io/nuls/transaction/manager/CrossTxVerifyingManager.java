package io.nuls.transaction.manager;

import io.nuls.base.data.NulsDigestData;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxStorageService;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.CrossChainTx;
import io.nuls.transaction.service.CrossChainTxService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理接收的其他链创建的跨链交易, 暂存验证中的跨链交易.
 *
 * @author: Charlie
 * @date: 2018/11/26
 */
@Service
public class CrossTxVerifyingManager {

    @Autowired
    private CrossChainTxService crossChainTxService;

    public void initCrossTxVerifyingMap(Chain chain) {
        //初始化跨链交易到缓存中
        List<CrossChainTx> txList = crossChainTxService.getTxList(chain);
        if (txList != null) {
            txList.forEach(tx -> chain.getCrossTxVerifyingMap().put(tx.getTx().getHash(), tx));
        }
    }

    public void putCrossChainTx(Chain chain, CrossChainTx crossChainTx) {
        chain.getCrossTxVerifyingMap().put(crossChainTx.getTx().getHash(), crossChainTx);
    }

    public boolean containsKey(Chain chain, NulsDigestData hash) {
        return chain.getCrossTxVerifyingMap().containsKey(hash);
    }

    public void removeCrossChainTx(Chain chain, NulsDigestData hash) {
        chain.getCrossTxVerifyingMap().remove(hash);
    }

    public Map<NulsDigestData, CrossChainTx> getCrossTxVerifyingMap(Chain chain) {
        return chain.getCrossTxVerifyingMap();
    }

    public boolean updateCrossChainTxState(Chain chain, NulsDigestData hash, int state) {
        boolean rs = false;
        if (chain.getCrossTxVerifyingMap().containsKey(hash)) {
            CrossChainTx crossChainTx = chain.getCrossTxVerifyingMap().get(hash);
            crossChainTx.setState(state);
            rs = true;
        }
        return rs;
    }
}
