package io.nuls.chain.storage.impl;

import io.nuls.chain.model.dto.ChainAsset;
import io.nuls.chain.storage.ChainAssetStorage;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
import static io.nuls.chain.util.LoggerUtil.Log;

/**
 * @author tangyi
 * @date 2018/11/12
 * @description
 */
@Component
public class ChainAssetStorageImpl implements ChainAssetStorage, InitializingBean {

    private final String TBL = "chain_asset";

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

    /**
     * Get specific asset values of a specific chain
     *
     * @param key chainId-assetId
     * @return ChainAsset object
     */
    @Override
    public ChainAsset load(String key) throws Exception {
        byte[] bytes = RocksDBService.get(TBL, key.getBytes());
        if (bytes == null) {
            return null;
        }

        ChainAsset chainAsset = new ChainAsset();
        chainAsset.parse(bytes, 0);
        return chainAsset;
    }

    /**
     * Save specific asset values of a specific chain
     *
     * @param key        chainId-assetId
     * @param chainAsset ChainAsset object
     */
    @Override
    public void save(String key, ChainAsset chainAsset) throws Exception {
        RocksDBService.put(TBL, key.getBytes(), chainAsset.serialize());
    }

    /**
     * Physical deletion
     *
     * @param key chainId-assetId
     */
    @Override
    public void delete(String key) throws Exception {
        RocksDBService.delete(TBL, key.getBytes());
    }

}
