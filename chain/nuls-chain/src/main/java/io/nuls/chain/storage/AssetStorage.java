package io.nuls.chain.storage;


import io.nuls.chain.model.dto.Asset;

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
     * @param key   ChainId_AssetId
     * @param asset Asset object that needs to be saved
     * @return true/false
     */
    boolean save(String key, Asset asset);

    /**
     * Find assets based on key
     *
     * @param key ChainId_AssetId
     * @return Asset object
     */
    Asset load(String key);

    /**
     * Physical deletion
     *
     * @param key ChainId_AssetId
     * @return true/false
     */
    boolean delete(String key);

    /**
     * Get all the assets of the chain
     *
     * @param chainId Chain ID
     * @return List of asset
     */
    List<Asset> getByChain(int chainId);


}
