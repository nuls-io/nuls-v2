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
     * @param key   Asset ID
     * @param asset Asset object that needs to be saved
     * @return true/false
     */
    boolean save(long key, Asset asset);

    /**
     * Find assets based on key
     *
     * @param key Asset ID
     * @return Asset object
     */
    Asset load(long key);

    /**
     * Physical deletion
     * @param key Asset ID
     */
    void delete(long key);

    /**
     * Get all the assets of the chain
     *
     * @param chainId Chain ID
     * @return List of asset
     */
    List<Asset> getByChain(short chainId);

    /**
     * Get asset by symbol
     * @param symbol Asset symbol
     * @return Asset object
     */
    Asset getBySymbol(String symbol);
}
