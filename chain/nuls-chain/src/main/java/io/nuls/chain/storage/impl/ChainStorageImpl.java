package io.nuls.chain.storage.impl;

import io.nuls.base.data.chain.Chain;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.storage.ChainStorage;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
@Component
public class ChainStorageImpl implements ChainStorage, InitializingBean {
    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() {

        try {
            RocksDBService.createTable(CmConstants.TBL_CHAIN);
        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                Log.error(e.getMessage());
                throw new NulsRuntimeException(CmConstants.DB_TABLE_CREATE_ERROR);
            }
        }
    }

    /**
     * Save chain
     *
     * @param key   The key
     * @param chain Chain object that needs to be saved
     * @return true/false
     */
    @Override
    public boolean save(short key, Chain chain) {
        try {
            return RocksDBService.put(CmConstants.TBL_CHAIN, ByteUtils.shortToBytes(key), chain.serialize());
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
    public Chain load(short key) {
        try {
            Chain chain = new Chain();
            byte[] bytes = RocksDBService.get(CmConstants.TBL_CHAIN, ByteUtils.shortToBytes(key));
            chain.parse(bytes, 0);
            return chain;
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }
    }
}
