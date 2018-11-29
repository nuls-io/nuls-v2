package io.nuls.chain.service.impl;

import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.ChainAsset;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.storage.AssetStorage;
import io.nuls.chain.storage.ChainAssetStorage;
import io.nuls.chain.storage.ChainStorage;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.thread.TimeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/9
 * @description
 */
@Component
public class AssetServiceImpl implements AssetService {

    @Autowired
    private AssetStorage assetStorage;

    @Autowired
    private ChainAssetStorage chainAssetStorage;

    @Autowired
    private ChainStorage chainStorage;

    /**
     * delete asset
     *
     * @param asset Asset object that needs to be delete
     * @return true/false
     */

    @Override
    public boolean deleteAsset(Asset asset) {
        String assetKey = CmRuntimeInfo.getAssetKey(asset.getChainId(),asset.getAssetId());
        String key = CmRuntimeInfo.getChainAssetKey(asset.getChainId(), assetKey);
        assetStorage.delete(assetKey);
        chainAssetStorage.delete(key);
        return true;
    }
    /**
     * Save asset
     *
     * @param asset Asset object that needs to be saved
     * @return true/false
     */
    @Override
    public boolean createAsset(Asset asset) {
        String assetKey = CmRuntimeInfo.getAssetKey(asset.getChainId(),asset.getAssetId());
        String key = CmRuntimeInfo.getChainAssetKey(asset.getChainId(), assetKey);
        asset.addChainId(asset.getChainId());
        boolean s1 = assetStorage.save(key, asset);
        ChainAsset chainAsset = new ChainAsset();
        chainAsset.setChainId(asset.getChainId());
        chainAsset.setAssetId(asset.getAssetId());
        chainAsset.setInitNumber(asset.getInitNumber());
        boolean s2 = chainAssetStorage.save(key, chainAsset);
        if (s1 && s2) {
            return true;
        } else {
            assetStorage.delete(key);
            chainAssetStorage.delete(key);
            return false;
        }
    }
    /**
     * saveOrUpdate chainAsset
     *
     *@param    chainAsset
     * @param    chainId
     * @return true/false
     */
    @Override
    public boolean saveOrUpdateChainAsset(int chainId,ChainAsset chainAsset) {
        String assetKey = CmRuntimeInfo.getAssetKey(chainAsset.getChainId(),chainAsset.getAssetId());
        String key = CmRuntimeInfo.getChainAssetKey(chainId, assetKey);
       return chainAssetStorage.save(key, chainAsset);
    }

    /**
     * update asset
     *
     * @param asset
     * @return
     */
    @Override
    public boolean updateAsset(Asset asset) {
        String assetKey = CmRuntimeInfo.getAssetKey(asset.getChainId(),asset.getAssetId());
        assetStorage.save(assetKey,asset);
        return true;
    }


    /**
     * Find asset based on key
     *
     * @param assetKey The asset key
     * @return Asset object
     */
    @Override
    public Asset getAsset(String assetKey) {
        return assetStorage.load(assetKey);
    }

    /**
     * Set the status of asset
     *
     * @param assetKey   The asset key
     * @param available The status of asset
     * @return true/false
     */
    @Override
    public boolean setStatus(String assetKey, boolean available) {
        Asset asset = assetStorage.load(assetKey);
        if (asset == null) {
            return false;
        }
        asset.setAvailable(available);
        asset.setLastUpdateTime(TimeService.currentTimeMillis());
        return assetStorage.save(assetKey, asset);
    }



    /**
     * Get all the assets of the chain
     *
     * @param chainId The chain ID
     * @return List of asset
     */
    @Override
    public List<Asset> getAssetByChain(int chainId) {
        return assetStorage.getByChain(chainId);
    }

    @Override
    public boolean assetExist(Asset asset) {
       Asset dbAsset =  assetStorage.load(CmRuntimeInfo.getAssetKey(asset.getChainId(),asset.getAssetId()));
        if(null !=dbAsset){
            return true;
        }
        return false;
    }



    /**
     * Verification of basic data
     *
     * @param asset Asset object
     * @return Error map
     */
    @Override
    public Map<String, String> basicValidator(Asset asset) {
        Map<String, String> errMap = new HashMap<>(16);
//        if (asset.getSymbol() == null) {
//            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_SYMBOL_NULL);
//        } else if (asset.getSymbol().length() > Integer.parseInt(CmConstants.PARAM_MAP.get(CmConstants.ASSET_SYMBOL_MAX))) {
//            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_SYMBOL_MAX);
//        }
//
//        if (asset.getName() == null) {
//            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_NAME_NULL);
//        } else if (asset.getName().length() > Integer.parseInt(CmConstants.PARAM_MAP.get(CmConstants.ASSET_NAME_MAX))) {
//            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_NAME_MAX);
//        }
//
//        if (asset.getDepositNuls() != Integer.parseInt(CmConstants.PARAM_MAP.get(CmConstants.ASSET_DEPOSITNULS))) {
//            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_DEPOSITNULS);
//        }
//        if (asset.getInitNumber() < Integer.parseInt(CmConstants.PARAM_MAP.get(CmConstants.ASSET_INITNUMBER_MIN))) {
//            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_INITNUMBER_MIN);
//        }
//        if (asset.getInitNumber() > Integer.parseInt(CmConstants.PARAM_MAP.get(CmConstants.ASSET_INITNUMBER_MAX))) {
//            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_INITNUMBER_MAX);
//        }
//        if (asset.getDecimalPlaces() < Integer.parseInt(CmConstants.PARAM_MAP.get(CmConstants.ASSET_DECIMALPLACES_MIN))) {
//            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_DECIMALPLACES_MIN);
//        }
//        if (asset.getDecimalPlaces() > Integer.parseInt(CmConstants.PARAM_MAP.get(CmConstants.ASSET_DECIMALPLACES_MAX))) {
//            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_DECIMALPLACES_MAX);
//        }

        return errMap;
    }


    /**
     * getChainAsset
     * @param asset Asset object
     * @return Error map
     */
    @Override
    public ChainAsset getChainAsset(int chainId,Asset asset) {
        ChainAsset chainAsset =chainAssetStorage.load(CmRuntimeInfo.getChainAssetKey(chainId,CmRuntimeInfo.getAssetKey(asset.getChainId(),asset.getAssetId())));
        return chainAsset;

    }

    @Override
    public ChainAsset getChainAsset(int chainId, String assetKey) {
        ChainAsset chainAsset =chainAssetStorage.load(CmRuntimeInfo.getChainAssetKey(chainId,assetKey));
        return chainAsset;
    }
}
