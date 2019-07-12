package io.nuls.chain.service;


import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.model.po.ChainAsset;

import java.math.BigInteger;
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
    void createAsset(Asset asset) throws Exception;

    /**
     * update asset
     *
     * @param asset
     * @return
     */
    void updateAsset(Asset asset) throws Exception;

    /**
     * @param assetMap
     * @throws Exception
     */
    void batchUpdateAsset(Map<String, Asset> assetMap) throws Exception;

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
     * @param assetKey  The asset key
     * @param available The status of asset
     * @return true/false
     */
    void setStatus(String assetKey, boolean available) throws Exception;


    List<Asset> getAssets(List<String> assetKeys) throws Exception;

    /**
     * juge asset exist in chain
     *
     * @param asset
     * @return
     */
    boolean assetExist(Asset asset) throws Exception;

    /**
     * juge asset exist in chain
     *
     * @param asset
     * @param map
     * @return
     * @throws Exception
     */
    boolean assetExist(Asset asset, Map<String, Integer> map) throws Exception;

    /**
     * @param chainId
     * @param assetKey
     * @return
     * @throws Exception
     */
    ChainAsset getChainAsset(int chainId, String assetKey) throws Exception;

    /**
     * @param chainAssetKey
     * @return
     * @throws Exception
     */
    ChainAsset getChainAsset(String chainAssetKey) throws Exception;

    /**
     * saveOrUpdate chainAsset
     *
     * @param chainId
     * @param chainAsset @return true/false
     */
    void saveOrUpdateChainAsset(int chainId, ChainAsset chainAsset) throws Exception;

    /**
     * @param chainAssets
     * @throws Exception
     */
    void batchSaveOrUpdateChainAsset(Map<String, ChainAsset> chainAssets) throws Exception;

    /**
     * @param key
     * @param amount
     * @throws Exception
     */
    void saveMsgChainCirculateAmount(String key, BigInteger amount) throws Exception;

    /**
     * 注册资产
     * Register asset
     *
     * @param asset
     * @param blockChains
     * @throws Exception Any error will throw an exception
     */
    void registerAsset(Asset asset, List<BlockChain> blockChains) throws Exception;

    /**
     * 回滚注册资产
     * Rollback the registered Asset
     *
     * @param asset The Asset be rollback
     * @throws Exception Any error will throw an exception
     */
    void registerAssetRollback(Asset asset) throws Exception;
}
