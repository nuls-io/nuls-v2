package io.nuls.chain.service.impl;

import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.BlockChain;
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
        BlockChain chain = getChain(chainId);
        if (null == chain) {
            chain = new BlockChain();
        }
        chain.setName(CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_CHAIN_NAME));
        int assetId = Integer.valueOf(CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_ASSET_ID));
        chain.setRegAssetId(assetId);
        chain.getSelfAssetKeyList().clear();
        chain.addCreateAssetId(CmRuntimeInfo.getAssetKey(chainId, assetId));
        chain.getTotalAssetKeyList().clear();
        chain.addCirculateAssetId(CmRuntimeInfo.getAssetKey(chainId, assetId));
        chainStorage.save(chainId, chain);
        Asset asset = assetService.getAsset(CmRuntimeInfo.getAssetKey(chainId, assetId));
        if (null == asset) {
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


    @Override
    public boolean saveChain(BlockChain blockChain) {
        return chainStorage.save(blockChain.getChainId(), blockChain);
    }


    @Override
    public boolean updateChain(BlockChain blockChain) {
        return chainStorage.update(blockChain.getChainId(), blockChain);
    }

    @Override
    public boolean delChain(BlockChain blockChain) {
        return chainStorage.delete(blockChain.getChainId());
    }

    /**
     * Find chain based on key
     *
     * @param chainId The chain ID
     * @return Chain
     */
    @Override
    public BlockChain getChain(int chainId) {
        return chainStorage.load(chainId);
    }


}
