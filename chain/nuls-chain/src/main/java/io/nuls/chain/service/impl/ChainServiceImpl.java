package io.nuls.chain.service.impl;

import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.ChainAsset;
import io.nuls.chain.model.txdata.Chain;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.storage.ChainAssetStorage;
import io.nuls.chain.storage.ChainStorage;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
@Service
public class ChainServiceImpl implements ChainService {

    @Autowired
    private ChainStorage chainStorage;

    @Autowired
    private ChainAssetStorage chainAssetStorage;

    /**
     * Save chain
     *
     * @param chain Chain object that needs to be saved
     * @return true/false
     */
    @Override
    public boolean saveChain(Chain chain) {
        return chainStorage.save(chain.getChainId(), chain);
    }

    /**
     * Find chain based on key
     *
     * @param chainId The chain ID
     * @return Chain
     */
    @Override
    public Chain getChain(short chainId) {
        Chain chain = chainStorage.load(chainId);
        List<ChainAsset> chainAssetList = getChainAssetByChain(chainId);
        chain.setChainAssetList(chainAssetList);
        return chain;
    }

    /**
     * Get asset information by chain ID
     *
     * @param chainId The chain ID
     * @return ChainAsset object
     */
    @Override
    public List<ChainAsset> getChainAssetByChain(short chainId) {
        return chainAssetStorage.getByChain(chainId);
    }

    /**
     * Get ChainAsset object
     *
     * @param chainId The chain ID
     * @param assetId The asset ID
     * @return ChainAsset object
     */
    @Override
    public ChainAsset getChainAsset(short chainId, long assetId) {
        String key = CmRuntimeInfo.getAssetKey(chainId, assetId);
        return chainAssetStorage.load(key);
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
    public boolean setAssetNumber(short chainId, long assetId, long currentNumber) {
        String key = CmRuntimeInfo.getAssetKey(chainId, assetId);
        ChainAsset chainAsset = chainAssetStorage.load(key);
        if (chainAsset == null) {
            return false;
        }
        chainAsset.setCurrentNumber(currentNumber);
        return chainAssetStorage.save(key, chainAsset);
    }
}
