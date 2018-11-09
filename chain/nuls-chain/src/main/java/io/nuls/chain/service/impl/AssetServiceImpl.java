package io.nuls.chain.service.impl;

import io.nuls.base.data.chain.Asset;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.storage.AssetStorage;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

/**
 * @author tangyi
 * @date 2018/11/9
 * @description
 */
@Component
public class AssetServiceImpl implements AssetService {

    @Autowired
    AssetStorage assetStorage;

    /**
     * Save asset
     *
     * @param asset Asset object that needs to be saved
     * @return 1 means success, 0 means failure
     */
    @Override
    public int saveAsset(Asset asset) {
        return assetStorage.save(asset);
    }

    /**
     * Find assets based on key
     *
     * @param assetId key
     * @return Asset object
     */
    @Override
    public Asset getAssetById(short assetId) {
        return assetStorage.load(assetId);
    }
}
