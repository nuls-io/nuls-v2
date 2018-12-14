package io.nuls.chain.service.impl;

import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.BlockChain;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
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
    public void saveChain(BlockChain blockChain) throws Exception {
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
    public void updateChain(BlockChain blockChain) throws Exception {
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
    public void delChain(BlockChain blockChain) throws Exception {
        chainStorage.delete(blockChain.getChainId());
    }

    /**
     * 根据序号获取链
     * Get the chain according to the ID
     *
     * @param chainId Chain ID
     * @return BlockChain
     * @throws Exception Any error will throw an exception
     */
    @Override
    public BlockChain getChain(int chainId) throws Exception {
        return chainStorage.load(chainId);
    }


    /**
     * 注册链
     * Register a new chain
     *
     * @param blockChain The BlockChain saved
     * @param asset      The Asset saved
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void registerBlockChain(BlockChain blockChain, Asset asset) throws Exception {
        /*
        1. 插入资产表
        2. 插入资产流通表
         */
        asset.addChainId(asset.getChainId());
        assetService.createAsset(asset);

        /*
        3. 插入链
         */
        blockChain.addCreateAssetId(CmRuntimeInfo.getAssetKey(blockChain.getChainId(), asset.getAssetId()));
        blockChain.addCirculateAssetId(CmRuntimeInfo.getAssetKey(blockChain.getChainId(), asset.getAssetId()));
        saveChain(blockChain);
    }

    /**
     * 回滚注册链
     * Rollback the registered BlockChain
     *
     * @param blockChain The rollback BlockChain
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void registerBlockChainRollback(BlockChain blockChain) throws Exception {
        delChain(blockChain);
        int assetId = blockChain.getRegAssetId();
        Asset asset = assetService.getAsset(CmRuntimeInfo.getAssetKey(blockChain.getChainId(), assetId));
        assetService.deleteAsset(asset);
    }

    /**
     * 销毁链
     * Destroy a exist BlockChain
     *
     * @param blockChain The BlockChain destroyed
     * @return The BlockChain after destroyed
     * @throws Exception Any error will throw an exception
     */
    @Override
    public BlockChain destroyBlockChain(BlockChain blockChain) throws Exception {
        //更新资产
        assetService.setStatus(CmRuntimeInfo.getAssetKey(blockChain.getChainId(), blockChain.getDelAssetId()), false);

        //更新链
        BlockChain dbChain = getChain(blockChain.getChainId());
        dbChain.setDelAddress(blockChain.getDelAddress());
        dbChain.setDelAssetId(blockChain.getDelAssetId());
        dbChain.setDelTxHash(blockChain.getDelTxHash());
        dbChain.removeCreateAssetId(CmRuntimeInfo.getAssetKey(blockChain.getChainId(), blockChain.getDelAssetId()));
        dbChain.setDelete(true);
        updateChain(dbChain);

        return dbChain;
    }

    /**
     * 回滚销毁的链
     * Rollback the destroyed BlockChain
     * @param dbChain The BlockChain need to be rollback
     * @throws Exception Any error will throw an exception
     */
    @Override
    public void destroyBlockChainRollback(BlockChain dbChain) throws Exception {
        //资产回滚
        String assetKey = CmRuntimeInfo.getAssetKey(dbChain.getChainId(), dbChain.getDelAssetId());
        assetService.setStatus(assetKey, true);
        //链回滚
        dbChain.setDelete(false);
        updateChain(dbChain);
    }
}
