package io.nuls.chain.service;


import io.nuls.chain.model.dto.Asset;

import java.util.List;
import java.util.Map;

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
//    boolean newAsset(Asset asset);

    /**
     * save asset
     *
     * @param asset
     * @return
     */
    boolean addAsset(Asset asset);

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
    List<Asset> getAssetByChain(int chainId);

    /**
     * juge asset exist in chain
     *
     * @param asset
     * @return
     */
    boolean assetExist(Asset asset);
    /**
     * Get asset by symbol
     * @param symbol Asset symbol
     * @return Asset object
     */
    Asset getAssetBySymbol(String symbol);

    /**
     * Verification of basic data
     * @param asset Asset object
     * @return Error map
     */
    Map<String,String> basicValidator(Asset asset);

    /**
     * Verification of unique data in db
     * @param asset Asset object
     * @return Error map
     */
    Map<String,String> uniqueValidator(Asset asset);
}
