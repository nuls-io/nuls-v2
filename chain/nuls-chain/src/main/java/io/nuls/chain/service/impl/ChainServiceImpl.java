package io.nuls.chain.service.impl;

import io.nuls.base.data.chain.Chain;
import io.nuls.base.data.chain.ChainAsset;
import io.nuls.chain.info.CmRuntimeInfo;
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
     * @param id The chain ID
     * @return Chain
     */
    @Override
    public Chain getChain(short id) {
        return chainStorage.load(id);
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
