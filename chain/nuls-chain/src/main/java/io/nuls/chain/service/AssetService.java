package io.nuls.chain.service;

import io.nuls.base.data.chain.Asset;

import java.util.List;

/**
 * @author tangyi
 */
public interface AssetService {

    /**
     * Save asset
     *
     * @param asset Asset object that needs to be saved
     * @return 1 means success, 0 means failure
     */
    int saveAsset(Asset asset);

    /**
     * Find asset based on key
     *
     * @param chainId The chain ID
     * @param assetId The asset ID
     * @return Asset object
     */
    Asset getAsset(short chainId, short assetId);

    /**
     * Set the status of asset
     *
     * @param chainId   The chain ID
     * @param assetId   The asset ID
     * @param available The status of asset
     * @return 1 means success, 0 means failure
     */
    int setStatus(short chainId, short assetId, boolean available);

    /**
     * Get all the assets of the chain
     *
     * @param chainId The chain ID
     * @return List of asset
     */
    List<Asset> getAssetListByChain(short chainId);

    /**
     * Set the currentNumber of asset
     *
     * @param chainId The chain ID
     * @param assetId The asset ID
     * @param currentNumber Current asset number in chain
     * @return 1 means success, 0 means failure
     */
    int setCurrentNumber(short chainId, short assetId, long currentNumber);
}
