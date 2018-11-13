package io.nuls.chain.service.impl;

import io.nuls.base.data.chain.Asset;
import io.nuls.base.data.chain.ChainAsset;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.storage.AssetStorage;
import io.nuls.chain.storage.ChainAssetStorage;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.Log;
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

    @Autowired
    private ChainAssetStorage chainAssetStorage;

    /**
     * Save asset
     *
     * @param asset Asset object that needs to be saved
     * @return true/false
     */
    @Override
    public boolean saveAsset(Asset asset) {
        try {
            assetStorage.save(asset.getAssetId(), asset);

            ChainAsset chainAsset = new ChainAsset();
            chainAsset.setChainId(asset.getChainId());
            chainAsset.setAssetId(asset.getAssetId());
            chainAsset.setCurrentNumber(asset.getInitNumber());
            String key = CmRuntimeInfo.getAssetKey(chainAsset.getChainId(), chainAsset.getAssetId());
            chainAssetStorage.save(key, chainAsset);

            return true;
        } catch (Exception e) {
            Log.error(e);
            assetStorage.delete(asset.getAssetId());
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
    public Asset getAsset(short assetId) {
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
    public boolean setStatus(short assetId, boolean available) {
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
    public List<Asset> getAssetListByChain(short chainId) {
        return assetStorage.getByChain(chainId);
    }

    /**
     * Set the currentNumber of asset
     *
     * @param chainId       The chain ID
     * @param assetId       The asset ID
     * @param currentNumber Current asset number in chain
     * @return true/false
     */
    @Override
    public boolean setCurrentNumber(short chainId, short assetId, long currentNumber) {
        String key = CmRuntimeInfo.getAssetKey(chainId, assetId);
        ChainAsset chainAsset = chainAssetStorage.load(key);
        if (chainAsset == null) {
            return false;
        }
        chainAsset.setCurrentNumber(currentNumber);
        return chainAssetStorage.save(key, chainAsset);
    }
}
