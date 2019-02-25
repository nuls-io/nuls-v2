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
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.ChainEventResult;
import io.nuls.chain.model.dto.CoinDataAssets;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.model.po.ChainAsset;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.TxCirculateService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.exception.NulsException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * @author lan
 * @description
 * @date 2019/02/19
 **/
@Service
public class TxCirculateServiceImpl implements TxCirculateService {

    @Autowired
    AssetService assetService;
    @Autowired
    ChainService chainService;

    @Override
    public List<CoinDataAssets> getChainAssetList(byte[] coinDataByte) throws NulsException {
        List<CoinDataAssets> list = new ArrayList<>();
        int fromChainId = 0;
        int toChainId = 0;
        Map<String, BigInteger> fromAssetMap = new HashMap<>(1);
        Map<String, BigInteger> toAssetMap = new HashMap<>(1);

        // 打造CoinData
        CoinData coinData = new CoinData();
        coinData.parse(coinDataByte, 0);

        // 从CoinData中取出from的资产信息，放入Map中（同类型相加）
        List<CoinFrom> listFrom = coinData.getFrom();
        for (CoinFrom coinFrom : listFrom) {
            fromChainId = AddressTool.getChainIdByAddress(coinFrom.getAddress());
            String assetKey = CmRuntimeInfo.getAssetKey(coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
            BigInteger amount = coinFrom.getAmount();
            BigInteger current = fromAssetMap.get(assetKey);
            if (current != null) {
                amount = amount.add(current);
            }
            fromAssetMap.put(assetKey, amount);
        }

        // 从CoinData中取出to的资产信息，放入Map中（同类型相加）
        List<CoinTo> listTo = coinData.getTo();
        for (CoinTo coinTo : listTo) {
            toChainId = AddressTool.getChainIdByAddress(coinTo.getAddress());
            int assetChainId = coinTo.getAssetsChainId();
            int assetId = coinTo.getAssetsId();
            String assetKey = CmRuntimeInfo.getAssetKey(assetChainId, assetId);
            BigInteger amount = coinTo.getAmount();
            BigInteger current = toAssetMap.get(assetKey);
            if (current != null) {
                amount = amount.add(current);
            }
            toAssetMap.put(assetKey, amount);
        }

        CoinDataAssets fromCoinDataAssets = new CoinDataAssets();
        fromCoinDataAssets.setChainId(fromChainId);
        fromCoinDataAssets.setAssetsMap(fromAssetMap);
        list.add(fromCoinDataAssets);
        CoinDataAssets toCoinDataAssets = new CoinDataAssets();
        toCoinDataAssets.setChainId(toChainId);
        toCoinDataAssets.setAssetsMap(toAssetMap);
        list.add(toCoinDataAssets);
        return list;

    }

    boolean isMainChain(int chainId) {
        return Integer.valueOf(CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_CHAIN_ID)) == chainId;
    }

    @Override
    public ChainEventResult circulateCommit(List<Transaction> txs) throws Exception {
        for (Transaction tx : txs) {
            List<CoinDataAssets> list = getChainAssetList(tx.getCoinData());
            CoinDataAssets fromCoinDataAssets = list.get(0);
            CoinDataAssets toCoinDataAssets = list.get(1);
            int fromChainId = fromCoinDataAssets.getChainId();
            int toChainId = toCoinDataAssets.getChainId();
            Map<String, BigInteger> fromAssetMap = fromCoinDataAssets.getAssetsMap();
            Map<String, BigInteger> toAssetMap = toCoinDataAssets.getAssetsMap();
            //from 的处理
            Set<String> assetKeys = fromAssetMap.keySet();
            for (String assetKey : assetKeys) {
                ChainAsset fromChainAsset = assetService.getChainAsset(fromChainId, assetKey);
                BigDecimal currentAsset = new BigDecimal(fromChainAsset.getOutNumber()).add(new BigDecimal(fromAssetMap.get(assetKey)));
                fromChainAsset.setOutNumber(BigIntegerUtils.stringToBigInteger(currentAsset.toString()));
                assetService.saveOrUpdateChainAsset(fromChainId, fromChainAsset);
            }
            if (!isMainChain(toChainId)) {
                //toChainId == nuls chain  需要进行跨外链的 手续费在coinBase里已经增加了。
                //toChainId != nuls chain 收取剩余x%的手续费
                //提取toChainId的 手续费资产，如果存将手续费放入外链给的回执，则这部分可以取消外链手续费的收取。
                String mainAssetKey = CmRuntimeInfo.getMainAsset();
                BigInteger allFromMainAmount = fromAssetMap.get(mainAssetKey);
                BigInteger allToMainAmount = toAssetMap.get(mainAssetKey);
                //40%的手续费归平台
                BigInteger feeAmount = (allFromMainAmount.subtract(allToMainAmount))
                        .multiply(new BigInteger("4").divide(new BigInteger("10")));
                if (null != toAssetMap.get(mainAssetKey)) {
                    feeAmount = feeAmount.add(toAssetMap.get(mainAssetKey));
                }
                toAssetMap.put(mainAssetKey, feeAmount);
            }
            //to 的处理
            Set<String> toAssetKeys = toAssetMap.keySet();
            for (String toAssetKey : toAssetKeys) {
                ChainAsset toChainAsset = assetService.getChainAsset(toChainId, toAssetKey);
                if (null == toChainAsset) {
                    //链下加资产，资产下增加链
                    BlockChain toChain = chainService.getChain(toChainId);
                    Asset asset = assetService.getAsset(CmRuntimeInfo.getMainAsset());
                    toChain.addCirculateAssetId(CmRuntimeInfo.getMainAsset());
                    asset.addChainId(toChainId);
                    chainService.updateChain(toChain);
                    assetService.updateAsset(asset);
                    //更新资产
                    toChainAsset = new ChainAsset();
                    toChainAsset.setAddressChainId(toChainId);
                    toChainAsset.setAssetChainId(asset.getChainId());
                    toChainAsset.setAssetId(asset.getAssetId());
                    toChainAsset.setInNumber(toAssetMap.get(toAssetKey));
                } else {
                    BigDecimal inAsset = new BigDecimal(toChainAsset.getInNumber());
                    BigDecimal inNumberBigDec = new BigDecimal(toAssetMap.get(toAssetKey)).add(inAsset);
                    toChainAsset.setInNumber(BigIntegerUtils.stringToBigInteger(inNumberBigDec.toString()));
                }
                assetService.saveOrUpdateChainAsset(toChainId, toChainAsset);
            }
        }
        return ChainEventResult.getResultSuccess();
    }

}
