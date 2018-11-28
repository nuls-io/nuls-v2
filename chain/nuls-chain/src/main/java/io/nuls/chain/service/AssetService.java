package io.nuls.chain.service;


import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.ChainAsset;

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
    boolean createAsset(Asset asset);

    /**
     * update asset
     *
     * @param asset
     * @return
     */
    boolean updateAsset(Asset asset);
    /**
     * delete asset
     *
     * @param asset
     * @return
     */
    boolean deleteAsset(Asset asset);



    /**
     * Find asset based on key
     *
     * @param assetKey The asset key
     * @return Asset object
     */
    Asset getAsset(String assetKey);

    /**
     * Set the status of asset
     *
     * @param assetKey   The asset key
     * @param available The status of asset
     * @return true/false
     */
    boolean setStatus(String assetKey, boolean available);

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
     * Verification of basic data
     * @param asset Asset object
     * @return Error map
     */
    Map<String,String> basicValidator(Asset asset);


    /**
     * getChainAsset
     * @param asset Asset object
     * @param chainId chainId
     * @return Error map
     */
    ChainAsset getChainAsset(int chainId,Asset asset);


    /**
     * saveOrUpdate chainAsset
     *
     *@param    chainAsset
     * @param    chainId
     * @return true/false
     */
    boolean saveOrUpdateChainAsset(int chainId,ChainAsset chainAsset) ;
}
