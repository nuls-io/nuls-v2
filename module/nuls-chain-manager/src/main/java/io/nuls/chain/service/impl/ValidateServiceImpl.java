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
import io.nuls.chain.config.NulsChainConfig;
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
import io.nuls.chain.util.ChainManagerUtil;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.log.Log;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.model.FormatValidUtils;

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
    @Autowired
    private NulsChainConfig nulsChainConfig;

    @Override
    public ChainEventResult assetDisableValidator(Asset asset) throws Exception {
        Asset dbAsset = assetService.getAsset(CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
        if (!ByteUtils.arrayEquals(asset.getAddress(), dbAsset.getAddress())) {
            LoggerUtil.logger().error("address={},dbAddr={} ERROR_ADDRESS_ERROR", AddressTool.getStringAddressByBytes(asset.getAddress()),
                    AddressTool.getStringAddressByBytes(dbAsset.getAddress()));
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ADDRESS_ERROR);
        }
//        if (!asset.getTxHash().equalsIgnoreCase(dbAsset.getTxHash())) {
//            LoggerUtil.logger().error("txHash={},dbHash={} ERROR_TX_HASH", asset.getTxHash(), dbAsset.getTxHash());
//            return ChainEventResult.getResultFail(CmErrorCode.ERROR_TX_HASH);
//        }
        if (asset.getChainId() != dbAsset.getChainId()) {
            LoggerUtil.logger().error("chainId={},dbChainId={} ERROR_CHAIN_ASSET_NOT_MATCH", asset.getChainId(), dbAsset.getChainId());
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_CHAIN_ASSET_NOT_MATCH);
        }
        ChainAsset chainAsset = assetService.getChainAsset(asset.getChainId(), CmRuntimeInfo.getAssetKey(dbAsset.getChainId(), dbAsset.getAssetId()));
        BigDecimal initAsset = new BigDecimal(chainAsset.getInitNumber());
        BigDecimal inAsset = new BigDecimal(chainAsset.getInNumber());
        BigDecimal outAsset = new BigDecimal(chainAsset.getOutNumber());
        BigDecimal currentNumber = initAsset.add(inAsset).subtract(outAsset);
        double actual = currentNumber.divide(initAsset, dbAsset.getDecimalPlaces(), RoundingMode.HALF_DOWN).doubleValue();
        double config = Double.parseDouble(nulsChainConfig.getAssetRecoveryRate());
        if (actual < config) {
            LoggerUtil.logger().error("chainId={},assetId={} actual={},config={},==={}-{}-{}", asset.getChainId(), asset.getAssetId(),
                    actual, config, initAsset, inAsset, outAsset);
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_RECOVERY_RATE);
        }
        return ChainEventResult.getResultSuccess();
    }

    @Override
    public ChainEventResult assetDisableValidatorV7(Asset asset) throws Exception {
        Asset dbAsset = assetService.getAsset(CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
        if (!ByteUtils.arrayEquals(asset.getAddress(), dbAsset.getAddress())) {
            LoggerUtil.logger().error("address={},dbAddr={} ERROR_ADDRESS_ERROR", AddressTool.getStringAddressByBytes(asset.getAddress()),
                    AddressTool.getStringAddressByBytes(dbAsset.getAddress()));
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ADDRESS_ERROR);
        }
        if (asset.getChainId() != dbAsset.getChainId()) {
            LoggerUtil.logger().error("chainId={},dbChainId={} ERROR_CHAIN_ASSET_NOT_MATCH", asset.getChainId(), dbAsset.getChainId());
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_CHAIN_ASSET_NOT_MATCH);
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
            LoggerUtil.logger().error("chainId={} ERROR_ASSET_NOT_EXIST", blockChain.getChainId());
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_NOT_EXIST);
        }
        if (!ByteUtils.arrayEquals(dbAsset.getAddress(), blockChain.getDelAddress())) {
            LoggerUtil.logger().error("chainId={} ERROR_ADDRESS_ERROR", blockChain.getChainId());
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ADDRESS_ERROR);
        }
        return ChainEventResult.getResultSuccess();
    }

    public ChainEventResult batchChainRegBaseValidator(BlockChain blockChain, Asset asset, Map<String, Integer> tempChains,
                                                       Map<String, Integer> tempAssets) throws Exception {
           /*
            判断链ID是否已经存在
            Determine if the chain ID already exists
             */
        BlockChain dbChain = chainService.getChain(blockChain.getChainId());
        boolean isChainDisable = (dbChain != null && dbChain.isDelete()) ? true : false;
        if (ChainManagerUtil.duplicateChainId(blockChain, tempChains) || chainService.chainExist(blockChain.getChainId())) {
            LoggerUtil.logger().error("chainId={} exist", blockChain.getChainId());
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_CHAIN_ID_EXIST);
        }
        if (ChainManagerUtil.duplicateMagicNumber(blockChain, tempChains) || (chainService.hadExistMagicNumber(blockChain.getMagicNumber()) && !isChainDisable)) {
            LoggerUtil.logger().error("magicNumber={} exist", blockChain.getMagicNumber());
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_MAGIC_NUMBER_EXIST);
        }
        if (ChainManagerUtil.duplicateChainName(blockChain, tempChains) || (chainService.hadExistChainName(blockChain.getChainName()) && !isChainDisable)) {
            LoggerUtil.logger().error("chainName={} exist", blockChain.getChainName());
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_CHAIN_NAME_EXIST);
        }
        if (assetService.regChainAssetExist(asset, tempAssets)) {
            LoggerUtil.logger().error("chainId={} assetId={} exist", asset.getChainId(), asset.getAssetId());
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_ID_EXIST);
        }
        if (0 == blockChain.getVerifierList().size()) {
            LoggerUtil.logger().error("chainId={} assetId={} getVerifierList=0", asset.getChainId(), asset.getAssetId());
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_VERIFIER_LIST_EMPTY);
        }
        if (CmConstants.MIN_SIGNATURE_BFT_RATIO > blockChain.getSignatureByzantineRatio()) {
            LoggerUtil.logger().error("chainId={} assetId={} getSignatureByzantineRatio={} less than {}", asset.getChainId(), asset.getAssetId(), blockChain.getSignatureByzantineRatio(), CmConstants.MIN_SIGNATURE_BFT_RATIO);
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_SIGNATURE_BYZANTINE_RATIO);
        }
        return ChainEventResult.getResultSuccess();
    }

    @Override
    public ChainEventResult batchChainRegValidator(BlockChain blockChain, Asset asset, Map<String, Integer> tempChains,
                                                   Map<String, Integer> tempAssets) throws Exception {
        ChainEventResult chainEventResult = batchChainRegBaseValidator(blockChain, asset, tempChains,
                tempAssets);

        return chainEventResult;
    }

    @Override
    public ChainEventResult batchChainRegValidatorV3(BlockChain blockChain, Asset asset, Map<String, Integer> tempChains,
                                                     Map<String, Integer> tempAssets) throws Exception {
        ChainEventResult chainEventResult = batchChainRegBaseValidator(blockChain, asset, tempChains,
                tempAssets);
        if (!chainEventResult.isSuccess()) {
            return chainEventResult;
        }
        if (asset.getDecimalPlaces() < Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMin()) || asset.getDecimalPlaces() > Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMax())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_DECIMALPLACES);
        }
        if (!FormatValidUtils.validTokenNameOrSymbol(asset.getSymbol())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_SYMBOL);
        }
        if (!FormatValidUtils.validTokenNameOrSymbol(asset.getAssetName())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_NAME);
        }
        //判断黑洞资产与锁定资产
        if (!BigIntegerUtils.isEqual(asset.getDepositNuls(), nulsChainConfig.getAssetDepositNuls())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_DEPOSITNULS);
        }
        if (!BigIntegerUtils.isEqual(asset.getDestroyNuls(), nulsChainConfig.getAssetDestroyNuls())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_DEPOSITNULS);
        }
        return ChainEventResult.getResultSuccess();
    }

    @Override
    public ChainEventResult batchAssetRegValidator(Asset asset, Map<String, Integer> tempAssets) throws Exception {
        if (assetService.assetExist(asset, tempAssets)) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_ID_EXIST);
        }
        if (asset.getDecimalPlaces() < Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMin()) || asset.getDecimalPlaces() > Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMax())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_DECIMALPLACES);
        }
        if (null == asset.getSymbol() || asset.getSymbol().length() > Integer.valueOf(nulsChainConfig.getAssetSymbolMax()) || asset.getSymbol().length() < 1) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_SYMBOL_LENGTH);
        }
        return ChainEventResult.getResultSuccess();
    }

    @Override
    public ChainEventResult batchAssetRegValidatorV3(Asset asset, Map<String, Integer> tempAssets) throws Exception {
        if (assetService.assetExist(asset, tempAssets)) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_ID_EXIST);
        }
        if (asset.getDecimalPlaces() < Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMin()) || asset.getDecimalPlaces() > Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMax())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_DECIMALPLACES);
        }
        if (!FormatValidUtils.validTokenNameOrSymbol(asset.getSymbol())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_SYMBOL);
        }
        if (!FormatValidUtils.validTokenNameOrSymbol(asset.getAssetName())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_NAME);
        }
        //判断黑洞资产与锁定资产
        if (!BigIntegerUtils.isEqual(asset.getDepositNuls(), nulsChainConfig.getAssetDepositNuls())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_DEPOSITNULS);
        }
        if (!BigIntegerUtils.isEqual(asset.getDestroyNuls(), nulsChainConfig.getAssetDestroyNuls())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_DEPOSITNULS);
        }
        return ChainEventResult.getResultSuccess();
    }

    @Override
    public ChainEventResult assetCirculateValidator(int fromChainId, int toChainId, Map<String, BigInteger> fromAssetMap, Map<String, BigInteger> toAssetMap) throws Exception {
        BlockChain fromChain = chainService.getChain(fromChainId);
        BlockChain toChain = chainService.getChain(toChainId);
        if (fromChainId == toChainId) {
            Log.error("fromChain ==toChain=={} is not cross tx", fromChain);
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_NOT_CROSS_TX);
        }
        if (fromChainId != 0 && fromChain.isDelete()) {
            Log.info("fromChain is delete,chainId={}", fromChain.getChainId());
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_CHAIN_NOT_FOUND);
        }
        if (toChainId != 0 && toChain.isDelete()) {
            Log.info("toChain is delete,chainId={}", toChain.getChainId());
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_CHAIN_NOT_FOUND);
        }
        //获取链内 资产 状态是否正常
        Set<String> toAssets = toAssetMap.keySet();
        for (Object toAsset : toAssets) {
            String assetKey = toAsset.toString();
            Asset asset = assetService.getAsset(assetKey);
            if (null == asset || !asset.isAvailable()) {
                Log.info("asset not exist", assetKey);
                return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_NOT_EXIST);
            }
        }
        //校验from 资产是否足够
        Set<String> fromAssets = fromAssetMap.keySet();
        for (Object fromAsset : fromAssets) {
            String assetKey = fromAsset.toString();
            Asset asset = assetService.getAsset(assetKey);
            if (null == asset || !asset.isAvailable()) {
                Log.info("asset not exist", assetKey);
                return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_NOT_EXIST);
            }
            ChainAsset chainAsset = assetService.getChainAsset(fromChainId, CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
            if (!chainAsset.isFromChainAsset()) {
                BigInteger currentAsset = chainAsset.getInitNumber().add(chainAsset.getInNumber()).subtract(chainAsset.getOutNumber());
                if (BigIntegerUtils.isLessThan(currentAsset, fromAssetMap.get(assetKey))) {
                    LoggerUtil.logger().error("fromChainId={},assetKey={}currentAsset={} fromAsset={} BALANCE_NOT_ENOUGH", fromChainId, assetKey, currentAsset, fromAssetMap.get(assetKey));
                    return ChainEventResult.getResultFail(CmErrorCode.BALANCE_NOT_ENOUGH);
                }
            }
        }
        return ChainEventResult.getResultSuccess();
    }

    @Override
    public ChainEventResult batchChainRegValidatorV7(BlockChain blockChain, Asset asset, Map<String, Integer> tempChains, Map<String, Integer> tempAssets) throws Exception {
        ChainEventResult chainEventResult = batchChainRegBaseValidator(blockChain, asset, tempChains, tempAssets);
        if (!chainEventResult.isSuccess()) {
            return chainEventResult;
        }
        if (asset.getDecimalPlaces() < Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMin()) || asset.getDecimalPlaces() > Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMax())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_DECIMALPLACES);
        }
        if (!FormatValidUtils.validTokenNameOrSymbol(asset.getSymbol())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_SYMBOL);
        }
        if (!FormatValidUtils.validTokenNameOrSymbol(asset.getAssetName())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_NAME);
        }
//        //判断黑洞资产与锁定资产
//        if(!BigIntegerUtils.isEqual(asset.getDepositNuls(),nulsChainConfig.getAssetDepositNuls())){
//            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_DEPOSITNULS);
//        }
//        if(!BigIntegerUtils.isEqual(asset.getDestroyNuls(),nulsChainConfig.getAssetDestroyNuls())){
//            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_DEPOSITNULS);
//        }
        return ChainEventResult.getResultSuccess();
    }

    @Override
    public ChainEventResult batchAssetRegValidatorV7(Asset asset, Map<String, Integer> tempAssets) throws Exception {
        if (assetService.assetExist(asset, tempAssets)) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_ID_EXIST);
        }
        if (asset.getDecimalPlaces() < Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMin()) || asset.getDecimalPlaces() > Integer.valueOf(nulsChainConfig.getAssetDecimalPlacesMax())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_DECIMALPLACES);
        }
        if (!FormatValidUtils.validTokenNameOrSymbol(asset.getSymbol())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_SYMBOL);
        }
        if (!FormatValidUtils.validTokenNameOrSymbol(asset.getAssetName())) {
            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_NAME);
        }
        //判断黑洞资产与锁定资产
//        if (!BigIntegerUtils.isEqual(asset.getDepositNuls(), nulsChainConfig.getAssetDepositNuls())) {
//            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_DEPOSITNULS);
//        }
//        if (!BigIntegerUtils.isEqual(asset.getDestroyNuls(), nulsChainConfig.getAssetDestroyNuls())) {
//            return ChainEventResult.getResultFail(CmErrorCode.ERROR_ASSET_DEPOSITNULS);
//        }
        return ChainEventResult.getResultSuccess();
    }
}
