package io.nuls.chain.storage;


import io.nuls.chain.model.po.ChainAsset;

public interface ChainAssetStorage {

    /**
     * Get specific asset values of a specific chain
     *
     * @param key chainId-assetId
     * @return ChainAsset object
     */
    ChainAsset load(String key) throws Exception;

    /**
     * Save specific asset values of a specific chain
     *
     * @param key        chainId-assetId
     * @param chainAsset ChainAsset object
     * @return true/false
     */
    void save(String key, ChainAsset chainAsset) throws Exception;

    /**
     * Physical deletion
     * @param key chainId-assetId
     * @return true/false
     */
    void delete(String key) throws Exception;

}
