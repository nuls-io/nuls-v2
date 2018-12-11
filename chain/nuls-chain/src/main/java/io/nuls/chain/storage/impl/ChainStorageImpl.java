package io.nuls.chain.storage.impl;

import io.nuls.chain.model.dto.BlockChain;
import io.nuls.chain.storage.ChainStorage;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
@Component
public class ChainStorageImpl implements ChainStorage, InitializingBean {

    private final String TBL = "block_chain";

    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() {
        try {
            if (!RocksDBService.existTable(TBL)) {
                RocksDBService.createTable(TBL);
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }


    @Override
    public boolean save(int key, BlockChain blockChain) {
        try {
            return RocksDBService.put(TBL, ByteUtils.intToBytes(key), blockChain.serialize());
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    @Override
    public boolean update(int key, BlockChain blockChain) {
        try {
            return RocksDBService.put(TBL, ByteUtils.intToBytes(key), blockChain.serialize());
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }
    /**
     * delete chain
     *
     * @param key   The key
     * @return true/false
     */
    @Override
    public boolean delete(int key) {
        try {
            return RocksDBService.delete(TBL, ByteUtils.intToBytes(key));
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * Find chain based on key
     *
     * @param key The key
     * @return Chain object
     */
    @Override
    public BlockChain load(int key) {
        byte[] bytes = RocksDBService.get(TBL, ByteUtils.intToBytes(key));
        if (bytes == null) {
            return null;
        }

        try {
            BlockChain blockChain = new BlockChain();
            blockChain.parse(bytes, 0);
            return blockChain;
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }
    }
}
