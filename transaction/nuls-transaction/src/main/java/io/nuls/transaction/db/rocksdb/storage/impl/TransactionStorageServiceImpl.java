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
import io.nuls.transaction.db.rocksdb.storage.TransactionStorageService;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.utils.DBUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/11/13
 */
@Service
public class TransactionStorageServiceImpl implements TransactionStorageService, InitializingBean {

    private static final String TRANSACTION_CONFIRMED = "transaction_confirmed";

    @Override
    public void afterPropertiesSet() throws NulsException {
        DBUtil.createTable(TRANSACTION_CONFIRMED);
    }

    @Override
    public boolean saveTx(int chainId, Transaction tx) {
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
            result = RocksDBService.put(TRANSACTION_CONFIRMED + chainId, txHashBytes, tx.serialize());
        } catch (Exception e) {
            Log.error(e);
        }
        return result;
    }

    @Override
    public boolean saveTxList(int chainId,List<Transaction> txList) {
        if (null == txList || txList.size() == 0) {
            throw new NulsRuntimeException(TxErrorCode.PARAMETER_ERROR);
        }
        Map<byte[], byte[]> txPoMap = new HashMap<>();
        try {
            for (Transaction tx : txList) {
                //序列化对象为byte数组存储
                txPoMap.put(tx.getHash().serialize(), tx.serialize());
            }
            return RocksDBService.batchPut(TRANSACTION_CONFIRMED+ chainId, txPoMap);
        } catch (Exception e) {
            Log.error(e.getMessage());
            throw new NulsRuntimeException(TxErrorCode.DB_SAVE_BATCH_ERROR);
        }
    }

    @Override
    public Transaction getTx(int chainId,NulsDigestData hash) {
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
        byte[] txBytes = RocksDBService.get(TRANSACTION_CONFIRMED+ chainId, hashBytes);
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
    public boolean removeTx(int chainId,NulsDigestData hash) {
        if (hash == null) {
            return false;
        }
        boolean result = false;
        try {
            result = RocksDBService.delete(TRANSACTION_CONFIRMED, hash.serialize());
        } catch (Exception e) {
            Log.error(e);
        }
        return result;
    }

    @Override
    public boolean removeTxList(int chainId, List<byte[]> hashList) {
        //check params
        if (hashList == null || hashList.size() == 0) {
            return false;
        }

        try {
            //delete transaction
            return RocksDBService.deleteKeys(TRANSACTION_CONFIRMED + chainId, hashList);
        } catch (Exception e) {
            Log.error(e);
        }
        return false;
    }

    @Override
    public List<Transaction> getTxList(int chainId, List<byte[]> hashList) {
        //check params
        if (hashList == null || hashList.size() == 0) {
            return null;
        }
        List<Transaction> txList = new ArrayList<>();
        //根据交易hash批量查询交易数据
        List<byte[]> list = RocksDBService.multiGetValueList(TRANSACTION_CONFIRMED + chainId, hashList);
        if (list != null) {
            for (byte[] value : list) {
                Transaction tx = new Transaction();
                try {
                    tx.parse(value, 0);
                } catch (NulsException e) {
                    Log.error(e);
                }
                txList.add(tx);
            }
        }
        return txList;
    }
}
