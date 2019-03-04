package io.nuls.chain.storage.impl;

import io.nuls.chain.model.po.ChainAsset;
import io.nuls.chain.storage.ChainAssetStorage;
import io.nuls.chain.storage.InitDB;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;

/**
 * @author tangyi
 * @date 2018/11/12
 * @description
 * key = CmRuntimeInfo.getChainAssetKey(chainId, CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId())
 * getAssetId = assetChainId+assetId
 * value = ChainAsset
 */
@Component
public class ChainAssetStorageImpl extends BaseStorage implements ChainAssetStorage,InitDB, InitializingBean {

    private final String TBL = "chain_asset";

    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() {

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

    @Override
    public void initTableName() throws NulsException {
        super.initTableName(TBL);
    }
}
