package io.nuls.transaction.db.rocksdb.storage.impl;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionManager;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.rocksdb.storage.TxVerifiedStorageService;
import io.nuls.transaction.model.bo.TxWrapper;

import java.io.IOException;

/**
 * 验证通过但未打包的交易
 * Save verified transaction (unpackaged)
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
@Service
public class TxVerifiedStorageServiceImpl implements TxVerifiedStorageService, InitializingBean {

    private final static String TRANSACTION_CACHE_KEY_NAME = "transactions_cache";

    @Override
    public void afterPropertiesSet() throws NulsException {
//        if (RocksDBService.existTable(TRANSACTION_CACHE_KEY_NAME)) {
//            try {
//                RocksDBService.destroyTable(TRANSACTION_CACHE_KEY_NAME);
//            } catch (Exception e) {
//                Log.error(e);
//                throw new NulsRuntimeException(TxErrorCode.DB_DELETE_ERROR);
//            }
//        }
//        try {
//            RocksDBService.createTable(TRANSACTION_CACHE_KEY_NAME);
//        } catch (Exception e) {
//            Log.error(e);
//            throw new NulsRuntimeException(TxErrorCode.DB_TABLE_CREATE_ERROR);
//        }
    }

    @Override
    public boolean putTx(TxWrapper txWrapper) {
        if (txWrapper == null) {
            return false;
        }
        Transaction tx = txWrapper.getTx();
        int chainId = txWrapper.getChainId();
        byte[] txHashBytes = null;
        try {
            txHashBytes = tx.getHash().serialize();
        } catch (IOException e) {
            Log.error(e);
            return false;
        }
        boolean result = false;
        try {
            result = RocksDBService.put(TRANSACTION_CACHE_KEY_NAME + chainId, txHashBytes, tx.serialize());
        } catch (Exception e) {
            Log.error(e);
        }
        return result;
    }


    @Override
    public Transaction getTx(int chainId, NulsDigestData hash) {
        if (hash == null) {
            return null;
        }
        byte[] hashBytes = null;
        try {
            hashBytes = hash.serialize();
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        byte[] txBytes = RocksDBService.get(TRANSACTION_CACHE_KEY_NAME + chainId, hashBytes);
        Transaction tx = null;
        if (null != txBytes) {
            try {
                tx = TransactionManager.getInstance(new NulsByteBuffer(txBytes, 0));
            } catch (Exception e) {
                Log.error(e);
                return null;
            }
        }
        return tx;
    }

    @Override
    public boolean removeTx(int chainId, NulsDigestData hash) {
        if (hash == null) {
            return false;
        }
        boolean result = false;
        try {
            result = RocksDBService.delete(TRANSACTION_CACHE_KEY_NAME + chainId, hash.serialize());
        } catch (Exception e) {
            Log.error(e);
        }
        return result;
    }
}
