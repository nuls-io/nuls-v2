package io.nuls.transaction.storage.impl;

import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.storage.TransactionCacheStorageService;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: Charlie
 * @date: 2018/11/13
 */
@Service
public class TransactionCacheStorageServiceImpl implements TransactionCacheStorageService, InitializingBean {
    private final static String TRANSACTION_CACHE_KEY_NAME = "transactions_cache";
    private final static byte[] LAST_KEY = "last_key".getBytes();
    private final static byte[] START_KEY = "start_key".getBytes();
    private int lastIndex = 0;
    private AtomicInteger startIndex = new AtomicInteger(0);

    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    @Override
    public void afterPropertiesSet() throws NulsException {
        RocksDBService.destroyArea(TRANSACTION_CACHE_KEY_NAME);
        Boolean result = RocksDBService.createTable(TRANSACTION_CACHE_KEY_NAME);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
        startIndex.set(1);
    }

    @Override
    public boolean putTx(Transaction tx) {
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
        // 保存交易
        Result result = null;
        try {
            result = dbService.put(TRANSACTION_CACHE_KEY_NAME, txHashBytes, tx.serialize());
        } catch (IOException e) {
            Log.error(e);
            return false;
        }

//        if(!result.isSuccess()) {
//            return result.isSuccess();
//        }
//        lastIndex++;
//        byte[] lastIndexBytes = Util.intToBytes(lastIndex);
//        result = dbService.put(TRANSACTION_CACHE_KEY_NAME, lastIndexBytes, txHashBytes);
//        if(!result.isSuccess()) {
//            removeTx(tx.getHash());
//            return result.isSuccess();
//        }
//        result = dbService.put(TRANSACTION_CACHE_KEY_NAME, LAST_KEY, lastIndexBytes);
        return result.isSuccess();
    }

    @Override
    public int getStartIndex() {
        byte[] lastIndexBytes = dbService.get(TRANSACTION_CACHE_KEY_NAME, START_KEY);
        if (lastIndexBytes == null) {
            return 0;
        }
        return ByteUtils.byteToInt(lastIndexBytes);
    }

    @Override
    public Transaction pollTx() {

        byte[] startIndexBytes = Util.intToBytes(startIndex.get());

        byte[] hashBytes = dbService.get(TRANSACTION_CACHE_KEY_NAME, startIndexBytes);
        if (hashBytes == null) {
            return null;
        }

        byte[] txBytes = dbService.get(TRANSACTION_CACHE_KEY_NAME, hashBytes);
        Transaction tx = null;
        if (null != txBytes) {
            try {
                tx = TransactionManager.getInstance(new NulsByteBuffer(txBytes, 0));
            } catch (Exception e) {
                Log.error(e);
                return null;
            }
        }

        startIndex.incrementAndGet();
//        dbService.put(TRANSACTION_CACHE_KEY_NAME, START_KEY, Util.intToBytes(startIndex));

        return tx;
    }

    @Override
    public Transaction getTx(NulsDigestData hash) {
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
        byte[] txBytes = dbService.get(TRANSACTION_CACHE_KEY_NAME, hashBytes);
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
    public boolean removeTx(NulsDigestData hash) {
        if (hash == null) {
            return false;
        }
        try {
            Result result = dbService.delete(TRANSACTION_CACHE_KEY_NAME, hash.serialize());
            return result.isSuccess();
        } catch (IOException e) {
            Log.error(e);
        }
        return false;
    }
}
