package io.nuls.transaction.service.impl;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxStorageService;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxUnprocessedStorageService;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.CrossChainTx;
import io.nuls.transaction.service.CrossChainTxService;

import java.util.List;

/**
 * @author: qinyifeng
 * @date: 2018/12/19
 */
@Service
public class CrossChainTxServiceImpl implements CrossChainTxService {

    @Autowired
    private CrossChainTxStorageService crossChainTxStorageService;
    @Autowired
    private CrossChainTxUnprocessedStorageService crossChainTxUnprocessedStorageService;

    @Override
    public void newCrossTx(Chain chain, String nodeId, Transaction tx) {
        if (tx == null) {
            return;
        }
        int chainId = chain.getChainId();
        //判断是否存在
        CrossChainTx ctxExist = crossChainTxUnprocessedStorageService.getTx(chainId, tx.getHash());
        if(null != ctxExist){
            return;
        }
        ctxExist = crossChainTxStorageService.getTx(chainId, tx.getHash());
        if(null != ctxExist){
            return;
        }
        CrossChainTx ctx = new CrossChainTx();
        ctx.setTx(tx);
        ctx.setSenderChainId(chainId);
        ctx.setSenderNodeId(nodeId);
        ctx.setState(TxConstant.CTX_UNPROCESSED_0);
        crossChainTxUnprocessedStorageService.putTx(chain.getChainId(), ctx);
    }

    @Override
    public boolean removeTx(Chain chain, NulsDigestData hash) {
        if (hash == null) {
            return false;
        }
        return crossChainTxStorageService.removeTx(chain.getChainId(), hash);
    }

    @Override
    public CrossChainTx getTx(Chain chain, NulsDigestData hash) {
        if (hash == null) {
            return null;
        }
        return crossChainTxStorageService.getTx(chain.getChainId(), hash);
    }

    @Override
    public List<CrossChainTx> getTxList(Chain chain) {
        return crossChainTxStorageService.getTxList(chain.getChainId());
    }

    @Override
    public boolean updateCrossTxState(Chain chain, NulsDigestData hash, int state) {
        CrossChainTx crossChainTx = crossChainTxStorageService.getTx(chain.getChainId(), hash);
        if(null != crossChainTx){
            chain.getLogger().error(hash.getDigestHex() + TxErrorCode.TX_NOT_EXIST.getMsg());
            return false;
        }
        crossChainTx.setState(state);
        return crossChainTxStorageService.putTx(chain.getChainId(),crossChainTx);
    }
}
