package io.nuls.chain.storage;


import io.nuls.chain.model.dto.ChainAsset;

import java.util.List;

public interface ChainAssetStorage {

    /**
     * Get specific asset values of a specific chain
     *
     * @param key chainId-assetId
     * @return ChainAsset object
     */
    ChainAsset load(String key);

    /**
     * Save specific asset values of a specific chain
     *
     * @param key        chainId-assetId
     * @param chainAsset ChainAsset object
     * @return true/false
     */
    boolean save(String key, ChainAsset chainAsset);

    /**
     * Physical deletion
     * @param key chainId-assetId
     * @return true/false
     */
    boolean delete(String key);

}
