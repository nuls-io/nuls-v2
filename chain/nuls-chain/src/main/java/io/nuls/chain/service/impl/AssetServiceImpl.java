package io.nuls.chain.service.impl;

import io.nuls.chain.info.CmConstants;
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
     * Save asset
     *
     * @param asset Asset object that needs to be saved
     * @return true/false
     */
    @Override
    public boolean addAsset(Asset asset) {
        boolean s1 = assetStorage.save(asset.getAssetId(), asset);
        String key = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
        ChainAsset chainAsset = new ChainAsset();
        chainAsset.setChainId(asset.getChainId());
        chainAsset.setAssetId(asset.getAssetId());
        chainAsset.setCurrentNumber(asset.getInitNumber());
        boolean s2 = chainAssetStorage.save(key, chainAsset);
        if (s1 && s2) {
            return true;
        } else {
            assetStorage.delete(asset.getAssetId());
            chainAssetStorage.delete(key);
            return false;
        }
    }

    /**
     * Find asset based on key
     *
     * @param assetId The asset ID
     * @return Asset object
     */
    @Override
    public Asset getAsset(long assetId) {
        return assetStorage.load(assetId);
    }

    /**
     * Set the status of asset
     *
     * @param assetId   The asset ID
     * @param available The status of asset
     * @return true/false
     */
    @Override
    public boolean setStatus(long assetId, boolean available) {
        Asset asset = assetStorage.load(assetId);
        if (asset == null) {
            return false;
        }
        asset.setAvailable(available);
        asset.setLastUpdateTime(TimeService.currentTimeMillis());
        return assetStorage.save(assetId, asset);
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
        Map<String, String> errorMap = uniqueValidator(asset);
        if(errorMap.size()> 0){
            return true;
        }
        return false;
    }

    /**
     * Get asset by symbol
     *
     * @param symbol Asset symbol
     * @return Asset object
     */
    @Override
    public Asset getAssetBySymbol(String symbol) {
        return assetStorage.getBySymbol(symbol);
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
        if (asset.getSymbol() == null) {
            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_SYMBOL_NULL);
        } else if (asset.getSymbol().length() > Integer.parseInt(CmConstants.PARAM_MAP.get(CmConstants.ASSET_SYMBOL_MAX))) {
            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_SYMBOL_MAX);
        }

        if (asset.getName() == null) {
            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_NAME_NULL);
        } else if (asset.getName().length() > Integer.parseInt(CmConstants.PARAM_MAP.get(CmConstants.ASSET_NAME_MAX))) {
            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_NAME_MAX);
        }

        if (asset.getDepositNuls() != Integer.parseInt(CmConstants.PARAM_MAP.get(CmConstants.ASSET_DEPOSITNULS))) {
            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_DEPOSITNULS);
        }
        if (asset.getInitNumber() < Integer.parseInt(CmConstants.PARAM_MAP.get(CmConstants.ASSET_INITNUMBER_MIN))) {
            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_INITNUMBER_MIN);
        }
        if (asset.getInitNumber() > Integer.parseInt(CmConstants.PARAM_MAP.get(CmConstants.ASSET_INITNUMBER_MAX))) {
            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_INITNUMBER_MAX);
        }
        if (asset.getDecimalPlaces() < Integer.parseInt(CmConstants.PARAM_MAP.get(CmConstants.ASSET_DECIMALPLACES_MIN))) {
            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_DECIMALPLACES_MIN);
        }
        if (asset.getDecimalPlaces() > Integer.parseInt(CmConstants.PARAM_MAP.get(CmConstants.ASSET_DECIMALPLACES_MAX))) {
            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_DECIMALPLACES_MAX);
        }

        return errMap;
    }

    /**
     * Verification of unique data in db
     *
     * @param asset Asset object
     * @return Error map
     */
    @Override
    public Map<String, String> uniqueValidator(Asset asset) {
        Map<String, String> errMap = new HashMap<>(16);
        if (getAsset(asset.getAssetId()) != null) {
            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_ID_EXIST);
        }
        if (getAssetBySymbol(asset.getSymbol()) != null) {
            CmRuntimeInfo.addError(errMap, CmConstants.ERROR_ASSET_SYMBOL_EXIST);
        }
        return errMap;
    }
}
