package io.nuls.transaction.service.impl;

import io.nuls.base.data.NulsDigestData;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.transaction.constant.TxDBConstant;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxStorageService;
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

    @Override
    public boolean putTx(int chainId, CrossChainTx ctx) {
        if (ctx == null) {
            return false;
        }
        return crossChainTxStorageService.putTx(chainId, ctx);
    }

    @Override
    public boolean removeTx(int chainId, NulsDigestData hash) {
        if (hash == null) {
            return false;
        }
        return crossChainTxStorageService.removeTx(chainId, hash);
    }

    @Override
    public CrossChainTx getTx(int chainId, NulsDigestData hash) {
        if (hash == null) {
            return null;
        }
        return crossChainTxStorageService.getTx(chainId, hash);
    }

    @Override
    public List<CrossChainTx> getTxList(int chainId) {
        return crossChainTxStorageService.getTxList(chainId);
    }
}
