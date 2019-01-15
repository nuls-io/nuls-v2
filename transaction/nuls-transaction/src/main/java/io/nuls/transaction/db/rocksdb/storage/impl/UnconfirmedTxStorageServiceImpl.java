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
import io.nuls.transaction.constant.TxDBConstant;
import io.nuls.transaction.db.rocksdb.storage.UnconfirmedTxStorageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 验证通过但未打包的交易
 * Save verified transaction (unpackaged)
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
@Service
public class UnconfirmedTxStorageServiceImpl implements UnconfirmedTxStorageService, InitializingBean {

    @Override
    public void afterPropertiesSet() throws NulsException {
    }

    @Override
    public boolean putTx(int chainId, Transaction tx) {
        if (tx == null) {
            return false;
        }
        byte[] txHashBytes = null;
        try {
            txHashBytes = tx.getHash().serialize();
        } catch (IOException e) {
            Log.error(e);
            return false;
        }
        boolean result = false;
        try {
            result = RocksDBService.put(TxDBConstant.DB_TRANSACTION_CACHE + chainId, txHashBytes, tx.serialize());
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
        byte[] txBytes = RocksDBService.get(TxDBConstant.DB_TRANSACTION_CACHE + chainId, hashBytes);
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
            result = RocksDBService.delete(TxDBConstant.DB_TRANSACTION_CACHE + chainId, hash.serialize());
        } catch (Exception e) {
            Log.error(e);
        }
        return result;
    }

    @Override
    public List<Transaction> getTxList(int chainId, List<byte[]> hashList) {
        //check params
        if (hashList == null || hashList.size() == 0) {
            return null;
        }
        List<Transaction> txList = new ArrayList<>();
        //根据交易hash批量查询交易数据
        List<byte[]> list = RocksDBService.multiGetValueList(TxDBConstant.DB_TRANSACTION_CACHE + chainId, hashList);
        if (list != null) {
            for (byte[] value : list) {
                Transaction transaction = new Transaction();
                try {
                    transaction.parse(value, 0);
                } catch (NulsException e) {
                    Log.error(e);
                }
                txList.add(transaction);
            }
        }
        return txList;
    }

    @Override
    public boolean removeTxList(int chainId, List<byte[]> hashList) {
        //check params
        if (hashList == null || hashList.size() == 0) {
            return false;
        }

        try {
            //delete transaction
            return RocksDBService.deleteKeys(TxDBConstant.DB_TRANSACTION_CACHE + chainId, hashList);
        } catch (Exception e) {
            Log.error(e);
        }
        return false;
    }
}
