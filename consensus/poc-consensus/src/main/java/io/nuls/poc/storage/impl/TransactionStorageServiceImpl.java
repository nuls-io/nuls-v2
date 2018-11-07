package io.nuls.poc.storage.impl;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionManager;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.db.service.RocksDBService;
import io.nuls.poc.storage.TransactionStorageService;
import io.nuls.poc.utils.ConsensusConstant;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

/**
 * 交易管理实现类
 * @author  tag
 * 2018/11/6
 * */
@Service
public class TransactionStorageServiceImpl implements TransactionStorageService, InitializingBean {
    @Override
    public boolean save(Transaction tx,int chainID) {
        if(tx == null || tx.getHash() == null){
            return false;
        }
        try {
            byte[] key = tx.getHash().serialize();
            byte[] value = tx.serialize();
            return RocksDBService.put(ConsensusConstant.DB_NAME_CONSUME_TX,key,value);
        }catch (Exception e){
            Log.error(e);
            return  false;
        }
    }

    @Override
    public Transaction get(NulsDigestData hash,int chainID) {
        if(hash == null){
            return  null;
        }
        try {
            byte[] key = hash.serialize();
            byte[] value = RocksDBService.get(ConsensusConstant.DB_NAME_CONSENSUS_AGENT,key);
            if(value == null){
                return  null;
            }
            return TransactionManager.getInstance(new NulsByteBuffer(value, 0));
        }catch (Exception e){
            Log.error(e);
            return  null;
        }
    }

    @Override
    public boolean delete(NulsDigestData hash,int chainID) {
        if(hash == null){
            return  false;
        }
        try {
            byte[] key = hash.serialize();
            return  RocksDBService.delete(ConsensusConstant.DB_NAME_CONSUME_TX,key);
        }catch (Exception e){
            Log.error(e);
            return  false;
        }
    }

    @Override
    public void afterPropertiesSet() throws NulsException {
        try {
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSUME_TX);
        }catch (Exception e){
            Log.error(e);
            throw new NulsException(e);
        }
    }
}
