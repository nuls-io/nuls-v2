package io.nuls.chain.service;


import io.nuls.chain.model.dto.ChainAsset;
import io.nuls.chain.model.txdata.Chain;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
public interface ChainService {
    /**
     * Save chain
     *
     * @param chain Chain object that needs to be saved
     * @return true/false
     */
    boolean saveChain(Chain chain);

    /**
     * Find chain based on key
     *
     * @param chainId The chain ID
     * @return Chain
     */
    Chain getChain(short chainId);

    /**
     * Get asset information by chain ID
     *
     * @param chainId The chain ID
     * @return ChainAsset object
     */
    List<ChainAsset> getChainAssetByChain(short chainId);

    /**
     * Get ChainAsset object
     *
     * @param chainId The chain ID
     * @param assetId The asset ID
     * @return ChainAsset object
     */
    ChainAsset getChainAsset(short chainId, long assetId);

    /**
     * Set the currentNumber of asset
     *
     * @param chainId       The chain ID
     * @param assetId       The asset ID
     * @param currentNumber Current asset number in chain
     * @return true/false
     */
    public boolean setAssetNumber(short chainId, long assetId, long currentNumber);
}
