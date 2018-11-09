package io.nuls.chain.service;

import io.nuls.base.data.chain.Asset;

/**
 * @author tangyi
 */
public interface AssetService {

    /**
     * Save asset
     * @param asset Asset object that needs to be saved
     * @return 1 means success, 0 means failure
     */
    int saveAsset(Asset asset);

    /**
     * Find assets based on key
     * @param assetId key
     * @return Asset object
     */
    Asset getAssetById(short assetId);
}
