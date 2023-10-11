/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.chain.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.po.*;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.CacheDataService;
import io.nuls.chain.storage.*;
import io.nuls.chain.util.ChainManagerUtil;
import io.nuls.chain.util.TxUtil;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.exception.NulsException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lan
 * @description
 * @date 2019/02/14
 **/
@Service
public class CacheDataServiceImpl implements CacheDataService {
    @Autowired
    BlockHeightStorage blockHeightStorage;
    @Autowired
    CacheDatasStorage cacheDatasStorage;
    @Autowired
    ChainStorage chainStorage;
    @Autowired
    ChainAssetStorage chainAssetStorage;
    @Autowired
    AssetService assetService;
    @Autowired
    AssetStorage assetStorage;

    @Override
    public void initBlockDatas() throws Exception {
        //获取确认高度
        BlockHeight blockHeight = blockHeightStorage.getBlockHeight(CmRuntimeInfo.getMainIntChainId());
        if (null != blockHeight) {
            if (!blockHeight.isCommit()) {
                CacheDatas cacheDatas = cacheDatasStorage.load(blockHeight.getBlockHeight() - 1);
                if (null != cacheDatas) {
                    rollDatas(cacheDatas.getBlockChains(), cacheDatas.getAssets(), cacheDatas.getChainAssets());
                }
                blockHeight.setBlockHeight(blockHeight.getBlockHeight() - 1);
                blockHeight.setCommit(true);
                blockHeightStorage.saveOrUpdateBlockHeight(CmRuntimeInfo.getMainIntChainId(), blockHeight);
            }
        }
    }

    public void rollDatas(List<BlockChain> blockChains, List<Asset> assets, List<ChainAsset> chainAssets) throws Exception {
        //取回滚的信息，进行回滚
        for (BlockChain blockChain : blockChains) {
            chainStorage.update(blockChain.getChainId(), blockChain);
        }
        for (Asset asset : assets) {
            assetStorage.save(CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()), asset);
        }
        for (ChainAsset chainAsset : chainAssets) {
            String assetKey = CmRuntimeInfo.getAssetKey(chainAsset.getAssetChainId(), chainAsset.getAssetId());
            chainAssetStorage.save(CmRuntimeInfo.getChainAssetKey(chainAsset.getAddressChainId(), assetKey), chainAsset);
        }
    }


    @Override
    public void bakBlockTxs(int chainId, long height, List<Transaction> txList, boolean isCirculate) throws Exception {
        Map<String, Integer> blockChains = new HashMap<>();
        Map<String, Integer> assets = new HashMap<>();
        Map<String, Integer> chainAssets = new HashMap<>();
        long preHeight = height - 1;
        //获取当前数据库高度的备份，进行merge
        CacheDatas moduleTxDatas = cacheDatasStorage.load(preHeight);
        if (null != moduleTxDatas) {
            //如果已经存在备份数据，则说明有业务（跨链交易）或者链注册管理交易 已经处理过了。
            return;
        }
        moduleTxDatas = new CacheDatas();
        //获取需要备份的链及资产
        if (isCirculate) {
            //构建跨链流通数据
            buildCirculateDatas(txList, blockChains, assets, chainAssets);
        } else {
            //构建链资产数据
            buildDatas(txList, blockChains, assets, chainAssets);
        }
        for (Map.Entry<String, Integer> entry : blockChains.entrySet()) {
            //缓存数据
            BlockChain blockChain = chainStorage.load(Integer.valueOf(entry.getKey()));
            moduleTxDatas.addBlockChain(blockChain);
        }
        for (Map.Entry<String, Integer> entry : assets.entrySet()) {
            //缓存数据
            Asset asset = assetStorage.load(entry.getKey());
            moduleTxDatas.addAsset(asset);
        }
        for (Map.Entry<String, Integer> entry : chainAssets.entrySet()) {
            //缓存数据
            ChainAsset chainAsset = assetService.getChainAsset(entry.getKey());
            moduleTxDatas.addChainAsset(chainAsset);
        }
        cacheDatasStorage.save(height, moduleTxDatas);
    }


    private void buildCirculateDatas(List<Transaction> txList, Map<String, Integer> blockChains, Map<String, Integer> assets, Map<String, Integer> chainAssets) throws NulsException {
        for (Transaction tx : txList) {
            int fromChainId = 0;
            int toChainId = 0;
            // 打造CoinData
            CoinData coinData = new CoinData();
            coinData.parse(tx.getCoinData(), 0);
            // 从CoinData中取出from的资产信息，放入Map中
            List<CoinFrom> listFrom = coinData.getFrom();
            for (CoinFrom coinFrom : listFrom) {
                fromChainId = AddressTool.getChainIdByAddress(coinFrom.getAddress());
                String assetKey = CmRuntimeInfo.getAssetKey(coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
                blockChains.put(String.valueOf(fromChainId), 1);
                chainAssets.put(CmRuntimeInfo.getChainAssetKey(fromChainId, assetKey), 1);
            }
            // 从CoinData中取出to的资产信息，放入Map中
            List<CoinTo> listTo = coinData.getTo();
            for (CoinTo coinTo : listTo) {
                toChainId = AddressTool.getChainIdByAddress(coinTo.getAddress());
                int assetChainId = coinTo.getAssetsChainId();
                int assetId = coinTo.getAssetsId();
                String assetKey = CmRuntimeInfo.getAssetKey(assetChainId, assetId);
                blockChains.put(String.valueOf(toChainId), 1);
                chainAssets.put(CmRuntimeInfo.getChainAssetKey(toChainId, assetKey), 1);
                assets.put(assetKey, 1);
            }
            assets.put(CmRuntimeInfo.getMainAssetKey(), 1);
            chainAssets.put(CmRuntimeInfo.getMainChainAssetKey(), 1);
            blockChains.put(CmRuntimeInfo.getMainChainId(), 1);
        }
    }

    private void buildDatas(List<Transaction> txList, Map<String, Integer> blockChains, Map<String, Integer> assets, Map<String, Integer> chainAssets) {
        for (Transaction tx : txList) {
            switch (tx.getType()) {
                case TxType.REGISTER_CHAIN_AND_ASSET:
                    BlockChain blockChain = null;
                    Asset asset = null;
                    if (ChainManagerUtil.getVersion(CmRuntimeInfo.getMainIntChainId()) >= CmConstants.LATEST_SUPPORT_VERSION) {
                        blockChain = TxUtil.buildChainWithTxDataV4(tx, false);
                        asset = TxUtil.buildAssetWithTxChainV4(tx);
                    } else {
                        blockChain = TxUtil.buildChainWithTxData(tx, false);
                        asset = TxUtil.buildAssetWithTxChain(tx);
                    }
                    blockChains.put(String.valueOf(blockChain.getChainId()), 1);
                    String assetKey = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
                    String key = CmRuntimeInfo.getChainAssetKey(asset.getChainId(), assetKey);
                    assets.put(assetKey, 1);
                    chainAssets.put(key, 1);
                    break;
                case TxType.DESTROY_CHAIN_AND_ASSET:
                    BlockChain delBlockChain = null;
                    if (ChainManagerUtil.getVersion(CmRuntimeInfo.getMainIntChainId()) >= CmConstants.LATEST_SUPPORT_VERSION) {
                        delBlockChain = TxUtil.buildChainWithTxDataV4(tx, true);
                    } else {
                        delBlockChain = TxUtil.buildChainWithTxData(tx, true);
                    }
                    blockChains.put(String.valueOf(delBlockChain.getChainId()), 1);
                    break;
                case TxType.ADD_ASSET_TO_CHAIN:
                    Asset addAsset = null;
                    if (ChainManagerUtil.getVersion(CmRuntimeInfo.getMainIntChainId()) >= CmConstants.LATEST_SUPPORT_VERSION) {
                        addAsset = TxUtil.buildAssetWithTxAssetV5(tx);
                    } else {
                        addAsset = TxUtil.buildAssetWithTxAsset(tx);
                    }
                    assets.put(CmRuntimeInfo.getAssetKey(addAsset.getChainId(), addAsset.getAssetId()), 1);
                    chainAssets.put(CmRuntimeInfo.getChainAssetKey(addAsset.getChainId(), CmRuntimeInfo.getAssetKey(addAsset.getChainId(), addAsset.getAssetId())), 1);
                    break;
                case TxType.REMOVE_ASSET_FROM_CHAIN:
                    Asset delAsset = null;
                    if (ChainManagerUtil.getVersion(CmRuntimeInfo.getMainIntChainId()) >= CmConstants.LATEST_SUPPORT_VERSION) {
                        delAsset = TxUtil.buildAssetWithTxAssetV5(tx);
                    } else {
                        delAsset = TxUtil.buildAssetWithTxAsset(tx);
                    }
                    assets.put(CmRuntimeInfo.getAssetKey(delAsset.getChainId(), delAsset.getAssetId()), 1);
                    chainAssets.put(CmRuntimeInfo.getChainAssetKey(delAsset.getChainId(), CmRuntimeInfo.getAssetKey(delAsset.getChainId(), delAsset.getAssetId())), 1);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void rollBlockTxs(int chainId, long height) throws Exception {
        //开始回滚
        BlockHeight saveBlockHeight = blockHeightStorage.getBlockHeight(chainId);
        if (null == saveBlockHeight) {
            return;
        }
        saveBlockHeight.setCommit(false);
        blockHeightStorage.saveOrUpdateBlockHeight(chainId, saveBlockHeight);
        //取回滚的信息，进行回滚
        CacheDatas moduleTxDatas = cacheDatasStorage.load(height - 1);
        if (null != moduleTxDatas) {
            for (BlockChain blockChain : moduleTxDatas.getBlockChains()) {
                chainStorage.update(blockChain.getChainId(), blockChain);
            }
            for (Asset asset : moduleTxDatas.getAssets()) {
                assetStorage.save(CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()), asset);
            }
            for (ChainAsset chainAsset : moduleTxDatas.getChainAssets()) {
                String assetKey = CmRuntimeInfo.getAssetKey(chainAsset.getAssetChainId(), chainAsset.getAssetId());
                chainAssetStorage.save(CmRuntimeInfo.getChainAssetKey(chainAsset.getAddressChainId(), assetKey), chainAsset);
            }
        }
        //提交回滚
        saveBlockHeight.setBlockHeight(height - 1);
        saveBlockHeight.setCommit(true);
        blockHeightStorage.saveOrUpdateBlockHeight(chainId, saveBlockHeight);
        //删除备份信息
        cacheDatasStorage.delete(height - 1);
    }

    @Override
    public CacheDatas getCacheDatas(long height) throws Exception {
        //取回滚的信息，进行回滚
        return cacheDatasStorage.load(height);
    }


    @Override
    public BlockHeight getBlockHeight(int chainId) throws Exception {
        BlockHeight blockHeight = blockHeightStorage.getBlockHeight(chainId);
        if (null == blockHeight) {
            return new BlockHeight();
        }
        return blockHeight;
    }

    @Override
    public void beginBakBlockHeight(int chainId, long blockHeight) throws Exception {
        // 存储区块高度 save block height
        BlockHeight saveBlockHeight = blockHeightStorage.getBlockHeight(chainId);
        if (null == saveBlockHeight) {
            saveBlockHeight = new BlockHeight();
        }
        saveBlockHeight.setCommit(false);
        saveBlockHeight.setBlockHeight(blockHeight);
        blockHeightStorage.saveOrUpdateBlockHeight(chainId, saveBlockHeight);
    }

    @Override
    public void endBakBlockHeight(int chainId, long blockHeight) throws Exception {
        // 存储区块高度 save block height
        BlockHeight saveBlockHeight = blockHeightStorage.getBlockHeight(chainId);
        saveBlockHeight.setCommit(true);
        blockHeightStorage.saveOrUpdateBlockHeight(chainId, saveBlockHeight);
    }
}
