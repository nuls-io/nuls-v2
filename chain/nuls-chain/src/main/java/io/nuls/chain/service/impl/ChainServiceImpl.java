package io.nuls.chain.service.impl;

import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.Chain;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.storage.ChainAssetStorage;
import io.nuls.chain.storage.ChainStorage;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;

import java.math.BigInteger;

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

    @Autowired
    private AssetService assetService;

    /**
     * init chain
     *
     * @return true/false
     */
    @Override
    public boolean initChain() {
        int chainId = Integer.valueOf(CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_CHAIN_ID));
        Chain chain = getChain(chainId);
        if(null == chain){
            chain = new Chain();
        }
        chain.setName(CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_CHAIN_NAME));
        int assetId = Integer.valueOf(CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_ASSET_ID));
        chain.setRegAssetId(assetId);
        chain.getAssetIds().clear();
        chain.addCreateAssetId(assetId);
        chain.getAssetsKey().clear();
        chain.addCirculateAssetId(CmRuntimeInfo.getAssetKey(chainId,assetId));
        chainStorage.save(chainId,chain);
        Asset asset = assetService.getAsset(CmRuntimeInfo.getAssetKey(chainId,assetId));
        if(null == asset){
            asset = new Asset();
        }
        asset.setChainId(chainId);
        asset.setAssetId(assetId);
        asset.setInitNumber(new BigInteger(CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_ASSET_MAX)));
        asset.setSymbol(CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_ASSET_SYMBOL));
        asset.addChainId(chainId);
        assetService.createAsset(asset);
        return true;
    }

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
     * updateChain
     *
     * @param chain
     * @return
     */
    @Override
    public  boolean updateChain(Chain chain){
        return chainStorage.update(chain.getChainId(), chain);
    }

    /**
     * delChain
     *
     * @param chain
     * @return
     */
    @Override
    public  boolean delChain(Chain chain){
        return chainStorage.delete(chain.getChainId());
    }

    /**
     * Find chain based on key
     *
     * @param chainId The chain ID
     * @return Chain
     */
    @Override
    public Chain getChain(int chainId) {
        Chain chain = chainStorage.load(chainId);
        return chain;
    }


}
