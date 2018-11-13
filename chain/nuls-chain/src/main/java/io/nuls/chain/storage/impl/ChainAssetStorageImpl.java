package io.nuls.chain.storage.impl;

import io.nuls.base.data.chain.ChainAsset;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.storage.ChainAssetStorage;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

/**
 * @author tangyi
 * @date 2018/11/12
 * @description
 */
@Component
public class ChainAssetStorageImpl implements ChainAssetStorage, InitializingBean {
    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() {
        try {
            RocksDBService.createTable(CmConstants.TBL_CHAIN_ASSET);
        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                Log.error(e.getMessage());
                throw new NulsRuntimeException(CmConstants.DB_TABLE_CREATE_ERROR);
            }
        }
    }

    /**
     * Get specific asset values of a specific chain
     *
     * @param key chainId-assetId
     * @return ChainAsset object
     */
    @Override
    public ChainAsset load(String key) {
        try {
            ChainAsset chainAsset = new ChainAsset();
            byte[] bytes = RocksDBService.get(CmConstants.TBL_CHAIN_ASSET, key.getBytes());
            chainAsset.parse(bytes, 0);
            return chainAsset;
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }
    }

    /**
     * Save specific asset values of a specific chain
     *
     * @param key        chainId-assetId
     * @param chainAsset ChainAsset object
     * @return true/false
     */
    @Override
    public boolean save(String key, ChainAsset chainAsset) {
        try {
            return RocksDBService.put(CmConstants.TBL_CHAIN, key.getBytes(), chainAsset.serialize());
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }
}
