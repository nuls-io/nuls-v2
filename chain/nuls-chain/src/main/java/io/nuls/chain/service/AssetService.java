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
     * @return true/false
     */
    boolean saveAsset(Asset asset);

    /**
     * Find asset based on key
     *
     * @param assetId The asset ID
     * @return Asset object
     */
    Asset getAsset(short assetId);

    /**
     * Set the status of asset
     *
     * @param assetId   The asset ID
     * @param available The status of asset
     * @return true/false
     */
    boolean setStatus(short assetId, boolean available);

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
     * @param chainId       The chain ID
     * @param assetId       The asset ID
     * @param currentNumber Current asset number in chain
     * @return true/false
     */
    boolean setCurrentNumber(short chainId, short assetId, long currentNumber);
}
