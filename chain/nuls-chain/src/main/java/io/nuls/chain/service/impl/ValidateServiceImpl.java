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

import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.ChainEventResult;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.model.po.ChainAsset;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.ValidateService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.model.ByteUtils;
import io.nuls.tools.log.Log;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lan
 * @description
 * @date 2019/02/20
 **/
@Service
public class ValidateServiceImpl implements ValidateService {
    @Autowired
    AssetService assetService;
    @Autowired
    ChainService chainService;
    @Override
    public ChainEventResult assetDisableValidator(Asset asset) throws Exception {
        Asset dbAsset = assetService.getAsset(CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));

        if (!ByteUtils.arrayEquals(asset.getAddress(), dbAsset.getAddress())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ADDRESS_ERROR);
        }
        if (!asset.getTxHash().equalsIgnoreCase(dbAsset.getTxHash())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_TX_HASH);
        }
        if (asset.getChainId() != dbAsset.getChainId()) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_CHAIN_ASSET_NOT_MATCH);
        }
        ChainAsset chainAsset = assetService.getChainAsset(asset.getChainId(), dbAsset);
        BigDecimal initAsset = new BigDecimal(chainAsset.getInitNumber());
        BigDecimal inAsset = new BigDecimal(chainAsset.getInNumber());
        BigDecimal outAsset = new BigDecimal(chainAsset.getOutNumber());
        BigDecimal currentNumber = initAsset.add(inAsset).subtract(outAsset);
        double actual = currentNumber.divide(initAsset, 8, RoundingMode.HALF_DOWN).doubleValue();
        double config = Double.parseDouble(CmConstants.PARAM_MAP.get(CmConstants.ASSET_RECOVERY_RATE));
        if (actual < config) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_RECOVERY_RATE);
        }
        return ChainEventResult.getResultSuccess();
    }

    @Override
    public ChainEventResult assetAddValidator(Asset asset) throws Exception {
        if (assetService.assetExist(asset)) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_ID_EXIST);
        }
        return ChainEventResult.getResultSuccess();
    }

    @Override
    public ChainEventResult chainAddValidator(BlockChain blockChain) throws Exception {
         /*
            判断链ID是否已经存在
            Determine if the chain ID already exists
             */
        if (chainService.chainExist(blockChain.getChainId())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_CHAIN_ID_EXIST);
        }
        return ChainEventResult.getResultSuccess();
    }

    @Override
    public ChainEventResult chainDisableValidator(BlockChain blockChain) throws Exception {
        if (null == blockChain) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_CHAIN_NOT_FOUND);
        }
        BlockChain dbChain = chainService.getChain(blockChain.getChainId());
        /*获取链下剩余的资产*/
        List<String> keys = dbChain.getSelfAssetKeyList();
        if (keys.size() == 0) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_CHAIN_ASSET_MUTI);
        }
        String key = keys.get(0);
        Asset dbAsset = assetService.getAsset(key);
        if (null == dbAsset) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_NOT_EXIST);
        }
        if (!ByteUtils.arrayEquals(dbAsset.getAddress(), blockChain.getDelAddress())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ADDRESS_ERROR);
        }
        return ChainEventResult.getResultSuccess();
    }

    @Override
    public ChainEventResult batchChainRegValidator(BlockChain blockChain, Asset asset, Map<String, Integer> tempChains, Map<String, Integer> tempAssets) throws Exception {
        /*
            判断链ID是否已经存在
            Determine if the chain ID already exists
             */
        if (chainService.chainExist(blockChain.getChainId(),tempChains)) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_CHAIN_ID_EXIST);
        }
        if(assetService.assetExist(asset,tempAssets)){
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_ID_EXIST);
        }
        return ChainEventResult.getResultSuccess();
    }

    @Override
    public ChainEventResult batchAssetRegValidator(Asset asset, Map<String, Integer> tempAssets) throws Exception {
        if(assetService.assetExist(asset,tempAssets)){
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_ID_EXIST);
        }
        return ChainEventResult.getResultSuccess();
    }

    @Override
    public ChainEventResult assetCirculateValidator(int fromChainId, int toChainId, Map<String, BigInteger > fromAssetMap, Map<String, BigInteger> toAssetMap) throws Exception {
                BlockChain fromChain = chainService.getChain(fromChainId);
                BlockChain toChain = chainService.getChain(toChainId);
                if (fromChainId == toChainId) {
                    Log.error("fromChain ==  toChain is not cross tx" + fromChain);
                    return ChainEventResult.getResultFail(CmErrorCode.ERROR_NOT_CROSS_TX);
                }
                if (fromChainId != 0 && fromChain.isDelete()) {
                    Log.info("fromChain is delete,chainId=" + fromChain.getChainId());
                    return ChainEventResult.getResultFail(CmErrorCode.ERROR_CHAIN_NOT_FOUND);
                }
                if (toChainId != 0 && toChain.isDelete()) {
                    Log.info("toChain is delete,chainId=" + fromChain.getChainId());
                    return ChainEventResult.getResultFail(CmErrorCode.ERROR_CHAIN_NOT_FOUND);
                }
                //获取链内 资产 状态是否正常
                Set<String> toAssets = toAssetMap.keySet();
                for (Object toAsset : toAssets) {
                    String assetKey = toAsset.toString();
                    Asset asset = assetService.getAsset(assetKey);
                    if (null == asset || !asset.isAvailable()) {
                        return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_NOT_EXIST);
                    }
                }
                //校验from 资产是否足够
                Set<String> fromAssets = fromAssetMap.keySet();
                for (Object fromAsset : fromAssets) {
                    String assetKey = fromAsset.toString();
                    Asset asset = assetService.getAsset(assetKey);
                    if (null == asset || !asset.isAvailable()) {
                        return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_NOT_EXIST);
                    }
                    ChainAsset chainAsset = assetService.getChainAsset(fromChainId, asset);
                    BigDecimal currentAsset = new BigDecimal(chainAsset.getInitNumber()).add(new BigDecimal(chainAsset.getInNumber())).subtract(new BigDecimal(chainAsset.getOutNumber()));
                    if (currentAsset.subtract(new BigDecimal(fromAssetMap.get(assetKey))).doubleValue() < 0) {
                        return ChainEventResult.getResultFail(CmErrorCode.BALANCE_NOT_ENOUGH);
                    }
                }
                return ChainEventResult.getResultSuccess();
    }
}
