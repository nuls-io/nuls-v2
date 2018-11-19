package io.nuls.transaction.db.rocksdb.storage.impl;

import io.nuls.base.data.NulsDigestData;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.transaction.model.bo.CrossChainTx;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxStorageService;
import io.nuls.transaction.utils.DBUtil;

import java.io.IOException;

/**
 * @author: Charlie
 * @date: 2018/11/13
 */
public class CrossChainTxStorageServiceImpl implements CrossChainTxStorageService, InitializingBean {

    private static final String TRANSACTION_CROSSCHAIN = "transaction_crosschain";

    @Override
    public void afterPropertiesSet() throws NulsException {
        DBUtil.createTable(TRANSACTION_CROSSCHAIN);
    }

    @Override
    public boolean putTx(CrossChainTx ctx) {
        if(null == ctx){
            return false;
        }
        byte[] txHashBytes = null;
        try {
            txHashBytes = ctx.getHash().serialize();
        } catch (IOException e) {
            Log.error(e);
            return false;
        }
        boolean result = false;
        try {
            result = RocksDBService.put(TRANSACTION_CROSSCHAIN, txHashBytes, ctx.serialize());
        } catch (Exception e) {
            Log.error(e);
        }
        return result;

    }

    @Override
    public CrossChainTx getTx(NulsDigestData hash) {
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
        byte[] txBytes = RocksDBService.get(TRANSACTION_CROSSCHAIN, hashBytes);

        if(null == txBytes){
            return null;
        }
        CrossChainTx tx = new CrossChainTx();
        try {
            tx.parse(txBytes, 0);
        } catch (NulsException e) {
            Log.error(e);
        }
        return tx;
    }

    @Override
    public boolean removeTx(NulsDigestData hash) {
        if (hash == null) {
            return false;
        }
        try {
            return RocksDBService.delete(TRANSACTION_CROSSCHAIN, hash.serialize());
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        } catch (Exception e) {
            Log.error(e);
            return false;
        }

    }
}
