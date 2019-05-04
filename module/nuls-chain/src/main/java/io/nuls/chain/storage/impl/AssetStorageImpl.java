package io.nuls.chain.storage.impl;

import io.nuls.chain.model.po.Asset;
import io.nuls.chain.storage.AssetStorage;
import io.nuls.chain.storage.InitDB;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/9
 * @description String assetKey = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
 * value =  Asset
 */
@Component
public class AssetStorageImpl extends  BaseStorage implements AssetStorage, InitDB, InitializingBean {

    private final String TBL = "asset";

    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() {

    }

    /**
     * Save asset
     *
     * @param key   The key
     * @param asset Asset object that needs to be saved
     */
    @Override
    public void save(String key, Asset asset) throws Exception {
        RocksDBService.put(TBL, key.getBytes(), asset.serialize());
    }

    /**
     * Find assets based on key
     *
     * @param key The key
     * @return Asset object
     */
    @Override
    public Asset load(String key) throws Exception {
        byte[] bytes = RocksDBService.get(TBL, key.getBytes());
        if (bytes == null) {
            return null;
        }

        Asset asset = new Asset();
        asset.parse(bytes, 0);
        return asset;
    }

    /**
     * Physical deletion
     *
     * @param key Asset ID
     */
    @Override
    public void delete(String key) throws Exception {
        RocksDBService.delete(TBL, key.getBytes());
    }

    /**
     * Get all the assets of the chain
     *
     * @param chainId The chain ID
     * @return List of asset
     */
    @Override
    public List<Asset> getByChain(int chainId) throws Exception {
        List<byte[]> bytesList = RocksDBService.valueList(TBL);
        List<Asset> assetList = new ArrayList<>();
        for (byte[] bytes : bytesList) {
            if (bytes == null) {
                continue;
            }

            Asset asset = new Asset();
            asset.parse(bytes, 0);
            if (asset.getChainId() == chainId) {
                assetList.add(asset);
            }
        }
        return assetList;
    }

    @Override
    public void initTableName() throws NulsException {
         super.initTableName(TBL);
    }
}
