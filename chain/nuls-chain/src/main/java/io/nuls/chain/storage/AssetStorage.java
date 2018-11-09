package io.nuls.chain.storage;

import io.nuls.base.data.chain.Asset;

/**
 * @author tangyi
 * @date 2018/11/9
 * @description
 */
public interface AssetStorage {

    /**
     * Save asset
     * @param asset Asset object that needs to be saved
     * @return 1 means success, 0 means failure
     */
    int save(Asset asset);

    /**
     * Find assets based on key
     * @param assetId key
     * @return Asset object
     */
    Asset load(short assetId);
}
