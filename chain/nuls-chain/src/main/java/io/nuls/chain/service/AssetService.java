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
    boolean newAsset(Asset asset);

    /**
     * Find asset based on key
     *
     * @param assetId The asset ID
     * @return Asset object
     */
    Asset getAsset(long assetId);

    /**
     * Set the status of asset
     *
     * @param assetId   The asset ID
     * @param available The status of asset
     * @return true/false
     */
    boolean setStatus(long assetId, boolean available);

    /**
     * Get all the assets of the chain
     *
     * @param chainId The chain ID
     * @return List of asset
     */
    List<Asset> getAssetByChain(short chainId);

    /**
     * Get asset by symbol
     * @param symbol Asset symbol
     * @return Asset object
     */
    Asset getAssetBySymbol(String symbol);
}
