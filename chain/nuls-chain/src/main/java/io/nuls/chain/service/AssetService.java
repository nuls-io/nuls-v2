package io.nuls.chain.service;


import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.ChainAsset;

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
//    boolean newAsset(Asset asset);

    /**
     * save asset
     *
     * @param asset
     * @return
     */
    void createAsset(Asset asset) throws Exception;

    /**
     * update asset
     *
     * @param asset
     * @return
     */
    void updateAsset(Asset asset) throws Exception;
    /**
     * delete asset
     *
     * @param asset
     * @return
     */
    void deleteAsset(Asset asset) throws Exception;



    /**
     * Find asset based on key
     *
     * @param assetKey The asset key
     * @return Asset object
     */
    Asset getAsset(String assetKey) throws Exception;

    /**
     * Set the status of asset
     *
     * @param assetKey   The asset key
     * @param available The status of asset
     * @return true/false
     */
    void setStatus(String assetKey, boolean available) throws Exception;

    /**
     * Get all the assets of the chain
     *
     * @param chainId The chain ID
     * @return List of asset
     */
    List<Asset> getAssetByChain(int chainId) throws Exception;

    /**
     * juge asset exist in chain
     *
     * @param asset
     * @return
     */
    boolean assetExist(Asset asset) throws Exception;

    /**
     * getChainAsset
     * @param asset Asset object
     * @param chainId chainId
     * @return Error map
     */
    ChainAsset getChainAsset(int chainId,Asset asset) throws Exception;

    /**
     *
     * @param chainId
     * @param assetKey
     * @return
     */
    ChainAsset getChainAsset(int chainId,String assetKey) throws Exception;

    /**
     * saveOrUpdate chainAsset
     *
     *
     * @param    chainId
     * @param    chainAsset  @return true/false
     */
    void saveOrUpdateChainAsset(int chainId, ChainAsset chainAsset) throws Exception;
}
