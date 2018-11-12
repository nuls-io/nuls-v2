package io.nuls.chain.service.impl;

import io.nuls.base.data.chain.Asset;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.storage.AssetStorage;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.thread.TimeService;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/9
 * @description
 */
@Component
public class AssetServiceImpl implements AssetService {

    @Autowired
    private AssetStorage assetStorage;

    /**
     * Save asset
     *
     * @param asset Asset object that needs to be saved
     * @return 1 means success, 0 means failure
     */
    @Override
    public int saveAsset(Asset asset) {
        String key = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
        return assetStorage.save(key, asset);
    }

    /**
     * Find asset based on key
     *
     * @param chainId The chain ID
     * @param assetId The asset ID
     * @return Asset object
     */
    @Override
    public Asset getAsset(short chainId, short assetId) {
        String key = CmRuntimeInfo.getAssetKey(chainId, assetId);
        return assetStorage.load(key);
    }

    /**
     * Set the status of asset
     *
     * @param chainId   The chain ID
     * @param assetId   The asset ID
     * @param available The status of asset
     * @return 1 means success, 0 means failure
     */
    @Override
    public int setStatus(short chainId, short assetId, boolean available) {
        String key = CmRuntimeInfo.getAssetKey(chainId, assetId);
        Asset asset = assetStorage.load(key);
        if (asset == null) {
            return 0;
        }
        asset.setAvailable(available);
        asset.setLastUpdateTime(TimeService.currentTimeMillis());
        return assetStorage.save(key, asset);
    }

    /**
     * Get all the assets of the chain
     *
     * @param chainId The chain ID
     * @return List of asset
     */
    @Override
    public List<Asset> getAssetListByChain(short chainId) {
        return assetStorage.getByChain(chainId);
    }

    /**
     * Set the currentNumber of asset
     *
     * @param chainId       The chain ID
     * @param assetId       The asset ID
     * @param currentNumber Current asset number in chain
     * @return 1 means success, 0 means failure
     */
    @Override
    public int setCurrentNumber(short chainId, short assetId, long currentNumber) {
        String key = CmRuntimeInfo.getAssetKey(chainId, assetId);
        Asset asset = assetStorage.load(key);
        if (asset == null) {
            return 0;
        }
        asset.setCurrentNumber(currentNumber);
        asset.setLastUpdateTime(TimeService.currentTimeMillis());
        return assetStorage.save(key, asset);
    }
}
