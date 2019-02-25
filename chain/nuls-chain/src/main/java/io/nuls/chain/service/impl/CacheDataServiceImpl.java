/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
import io.nuls.chain.info.ChainTxConstants;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.po.*;
import io.nuls.chain.service.CacheDataService;
import io.nuls.chain.storage.*;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.chain.util.TxUtil;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;

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
    AssetStorage assetStorage;
    @Override
    public void initBlockDatas() throws Exception {
        //获取确认高度
        BlockHeight blockHeight = blockHeightStorage.getBlockHeight(CmRuntimeInfo.getMainIntChainId(), false);
        if (null != blockHeight) {
            if(!blockHeight.isCommit()) {
                CacheDatas cacheDatas = cacheDatasStorage.load(blockHeight.getBlockHeight());
                rollDatas(cacheDatas.getBlockChains(),cacheDatas.getAssets(),cacheDatas.getChainAssets());
                int size = blockHeight.getBakHeighList().size()-1;
                if(blockHeight.getBakHeighList().get(size) == blockHeight.getBlockHeight()){
                    blockHeight.getBakHeighList().remove(size);
                }

                blockHeight.setBlockHeight(cacheDatas.getPreBlockHeight());
                blockHeight.setCommit(true);
                blockHeightStorage.saveOrUpdateBlockHeight(CmRuntimeInfo.getMainIntChainId(),blockHeight,false);
            }
        }
    }
    public void rollDatas(List<BlockChain> blockChains, List<Asset> assets,List<ChainAsset> chainAssets) throws Exception {
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
    public void bakBlockTxs(int chainId, long preHeight, long height, List<Transaction> txList, boolean isCirculate) throws Exception {
        Map<String, Integer> blockChains = new HashMap<>();
        Map<String, Integer> assets = new HashMap<>();
        Map<String, Integer> chainAssets = new HashMap<>();
        //获取需要备份的链及资产
        if(isCirculate){
            buildCirculateDatas(txList, blockChains, assets, chainAssets);
        }else {
            buildDatas(txList, blockChains, assets, chainAssets);
        }
        CacheDatas moduleTxDatas = new CacheDatas();
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
            ChainAsset chainAsset = chainAssetStorage.load(entry.getKey());
            moduleTxDatas.addChainAsset(chainAsset);
        }
        //存储当前高度交易涉及的数据的前一个状态
        moduleTxDatas.setPreBlockHeight(preHeight);
        cacheDatasStorage.save(height, moduleTxDatas);
        //删除多余的数据
        clearMoreBakDatas(chainId);
    }

    private void clearMoreBakDatas(int chainId) throws Exception {
        //删除多余的数据
        BlockHeight blockHeight = blockHeightStorage.getBlockHeight(chainId, false);
        if (null != blockHeight) {
            if (blockHeight.getBakHeighList().size() > CmConstants.BAK_BLOCK_MAX_COUNT) {
                cacheDatasStorage.delete(blockHeight.getBakHeighList().get(0));
                blockHeight.getBakHeighList().remove(0);
                blockHeightStorage.saveOrUpdateBlockHeight(chainId, blockHeight, true);
            }
        }
    }
    private void buildCirculateDatas(List<Transaction> txList, Map<String, Integer> blockChains,Map<String, Integer> assets, Map<String, Integer> chainAssets) throws NulsException {
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
                assets.put(assetKey,1);
            }
            assets.put(CmRuntimeInfo.getMainAsset(),1);
            chainAssets.put(CmRuntimeInfo.getMainChainAssetKey(),1);
            blockChains.put(CmRuntimeInfo.getMainChainId(),1);
        }
    }

    private void buildDatas(List<Transaction> txList, Map<String, Integer> blockChains, Map<String, Integer> assets, Map<String, Integer> chainAssets) {
        for (Transaction tx : txList) {
            switch (tx.getType()) {
                case ChainTxConstants.TX_TYPE_REGISTER_CHAIN_AND_ASSET:
                    BlockChain blockChain = TxUtil.buildChainWithTxData(tx, false);
                    Asset asset = TxUtil.buildAssetWithTxChain(tx);
                    blockChains.put(String.valueOf(blockChain.getChainId()), 1);
                    String assetKey = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
                    String key = CmRuntimeInfo.getChainAssetKey(asset.getChainId(), assetKey);
                    assets.put(assetKey, 1);
                    chainAssets.put(key, 1);
                    break;
                case ChainTxConstants.TX_TYPE_DESTROY_ASSET_AND_CHAIN:
                    BlockChain blockChain2 = TxUtil.buildChainWithTxData(tx, true);
                    blockChains.put(String.valueOf(blockChain2.getChainId()), 1);
                    break;
                case ChainTxConstants.TX_TYPE_ADD_ASSET_TO_CHAIN:
                    Asset asset2 = TxUtil.buildAssetWithTxChain(tx);
                    assets.put(CmRuntimeInfo.getAssetKey(asset2.getChainId(), asset2.getAssetId()), 1);
                    chainAssets.put(CmRuntimeInfo.getChainAssetKey(asset2.getChainId(), CmRuntimeInfo.getAssetKey(asset2.getChainId(), asset2.getAssetId())), 1);
                    break;
                case ChainTxConstants.TX_TYPE_REMOVE_ASSET_FROM_CHAIN:
                    Asset asset3 = TxUtil.buildAssetWithTxChain(tx);
                    assets.put(CmRuntimeInfo.getAssetKey(asset3.getChainId(), asset3.getAssetId()), 1);
                    chainAssets.put(CmRuntimeInfo.getChainAssetKey(asset3.getChainId(), CmRuntimeInfo.getAssetKey(asset3.getChainId(), asset3.getAssetId())), 1);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void rollBlockTxs(int chainId, long height) throws Exception {
        //开始回滚
        BlockHeight saveBlockHeight = blockHeightStorage.getBlockHeight(chainId, false);
        saveBlockHeight.setCommit(false);
        blockHeightStorage.saveOrUpdateBlockHeight(chainId,saveBlockHeight,false);
        //取回滚的信息，进行回滚
        CacheDatas moduleTxDatas = cacheDatasStorage.load(height);
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
        //提交回滚
        saveBlockHeight.setBlockHeight(moduleTxDatas.getPreBlockHeight());
        int size = saveBlockHeight.getBakHeighList().size();
        saveBlockHeight.getBakHeighList().remove(saveBlockHeight.getBakHeighList().get(size-1));
        saveBlockHeight.setCommit(true);
        blockHeightStorage.saveOrUpdateBlockHeight(chainId,saveBlockHeight,false);
        //删除备份信息
        cacheDatasStorage.delete(height);
    }

    @Override
    public CacheDatas getCacheDatas(long height) throws Exception {
        //取回滚的信息，进行回滚
        return cacheDatasStorage.load(height);
    }


    @Override
    public BlockHeight getBlockHeight(int chainId) throws Exception {
        BlockHeight blockHeight = blockHeightStorage.getBlockHeight(chainId, false);
        if (null == blockHeight) {
            return new BlockHeight();
        }
        return blockHeight;
    }

    @Override
    public void beginBakBlockHeight(int chainId, long blockHeight) throws Exception {
        // 存储区块高度 save block height
        BlockHeight saveBlockHeight = blockHeightStorage.getBlockHeight(chainId, false);
        if (null == saveBlockHeight) {
            saveBlockHeight = new BlockHeight();
        }

        if (!saveBlockHeight.isCommit()) {
            LoggerUtil.Log.error("Data conflict,Block is unCommit error.chainId={},blockHeight={}", chainId, blockHeight);
            throw new Exception("Data conflict,Block is unCommit error.");
        }
        saveBlockHeight.addBakHeight(blockHeight);
        saveBlockHeight.setCommit(false);
        saveBlockHeight.setBlockHeight(blockHeight);
        blockHeightStorage.saveOrUpdateBlockHeight(chainId, saveBlockHeight, false);
    }

    @Override
    public void endBakBlockHeight(int chainId, long blockHeight) throws Exception {
        // 存储区块高度 save block height
        BlockHeight saveBlockHeight = blockHeightStorage.getBlockHeight(chainId, false);
        saveBlockHeight.setCommit(true);
        blockHeightStorage.saveOrUpdateBlockHeight(chainId, saveBlockHeight, false);
    }
}
