package io.nuls.chain.storage;

import io.nuls.base.data.chain.Asset;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/9
 * @description
 */
public interface AssetStorage {

    /**
     * Save asset
     *
     * @param key   The key
     * @param asset Asset object that needs to be saved
     * @return 1 means success, 0 means failure
     */
    int save(String key, Asset asset);

    /**
     * Find assets based on key
     *
     * @param key The key
     * @return Asset object
     */
    Asset load(String key);


    /**
     * Get all the assets of the chain
     *
     * @param chainId The chain ID
     * @return List of asset
     */
    List<Asset> getByChain(short chainId);
}
