package io.nuls.chain.service.impl;

import io.nuls.base.data.chain.Asset;
import io.nuls.base.data.chain.Chain;
import io.nuls.base.data.chain.ChainAsset;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.storage.AssetStorage;
import io.nuls.chain.storage.ChainAssetStorage;
import io.nuls.chain.storage.ChainStorage;
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
    public boolean newAsset(Asset asset) {
        String key = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());

        boolean s1 = assetStorage.save(asset.getAssetId(), asset);

        Chain chain = chainStorage.load(asset.getChainId());
        ChainAsset chainAsset = new ChainAsset();
        chainAsset.setChainId(asset.getChainId());
        chainAsset.setAssetId(asset.getAssetId());
        chainAsset.setCurrentNumber(asset.getInitNumber());
        chainAsset.setChainName(chain.getName());
        chainAsset.setAssetSymbol(asset.getSymbol());
        chainAsset.setAssetName(asset.getName());
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
    public List<Asset> getAssetByChain(short chainId) {
        return assetStorage.getByChain(chainId);
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
}
