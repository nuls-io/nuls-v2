package io.nuls.transaction.service.impl;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxDBConstant;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxStorageService;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxUnprocessedStorageService;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.CrossChainTx;
import io.nuls.transaction.service.CrossChainTxService;

import java.io.IOException;
import java.util.ArrayList;
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
    public boolean newCrossTx(Chain chain, String nodeId, Transaction tx) {
        if (tx == null) {
            return false;
        }
        CrossChainTx ctx = new CrossChainTx();
        ctx.setTx(tx);
        ctx.setNodeId(nodeId);
        ctx.setState(TxConstant.CTX_UNPROCESSED_0);
        return crossChainTxUnprocessedStorageService.putTx(chain.getChainId(), ctx);
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
        return false;
    }
}
