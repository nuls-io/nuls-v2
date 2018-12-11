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
 * 关于链的所有操作：增删改查
 * All operations on the chain: Save, delete, update, query
 *
 * @author tangyi
 * @date 2018/11/8
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
     * 把Nuls2.0主网默认注册到Nuls2.0上（Nuls2.0主网可以认为是Nuls2.0生态的第一条友链）
     * Register the Nuls2.0 main network to Nuls2.0 by default (Nuls2.0 main network can be considered as the first friend chain of Nurs2.0 ecosystem)
     *
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void initChain() throws Exception {
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
    }

    /**
     * 保存链信息
     * Save chain
     *
     * @param blockChain The BlockChain saved
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void saveChain(BlockChain blockChain) {
        chainStorage.save(blockChain.getChainId(), blockChain);
    }

    /**
     * 更新链信息
     * Update chain
     *
     * @param blockChain The BlockChain updated
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void updateChain(BlockChain blockChain) {
        chainStorage.update(blockChain.getChainId(), blockChain);
    }

    /**
     * 删除链信息
     * Delete chain
     *
     * @param blockChain The BlockChain deleted
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void delChain(BlockChain blockChain) {
        chainStorage.delete(blockChain.getChainId());
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
