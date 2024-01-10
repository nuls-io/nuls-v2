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
package io.nuls.chain.rpc.cmd;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.RpcConstants;
import io.nuls.chain.model.dto.ChainAssetTotalCirculate;
import io.nuls.chain.model.dto.ChainEventResult;
import io.nuls.chain.model.dto.CoinDataAssets;
import io.nuls.chain.model.po.CacheDatas;
import io.nuls.chain.model.po.ChainAsset;
import io.nuls.chain.service.*;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.chain.util.TxUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.model.ObjectUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processing of asset cross transaction circulation
 *
 * @author lan
 * @date 2019/02/21
 **/
@Component
@NulsCoresCmd(module = ModuleE.CM)
public class TxCirculateCmd extends BaseChainCmd {


    @Autowired
    private AssetService assetService;
    @Autowired
    private ChainService chainService;
    @Autowired
    private ValidateService validateService;
    @Autowired
    private CacheDataService cacheDataService;
    @Autowired
    private TxCirculateService txCirculateService;
    @Autowired
    private MessageService messageService;

    /**
     * Query on chain assets
     */
    @CmdAnnotation(cmd = RpcConstants.CMD_GET_CIRCULATE_CHAIN_ASSET, version = 1.0,
            description = "Query asset information")
    @Parameters(value = {
            @Parameter(parameterName = "circulateChainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "Running ChainID,Value range[1-65535]"),
            @Parameter(parameterName = "assetChainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "Asset ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "assetId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "assetId,Value range[1-65535]")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "circulateChainId", valueType = Integer.class, description = "Running ChainID"),
                    @Key(name = "assetChainId", valueType = Integer.class, description = "Asset ChainID"),
                    @Key(name = "assetId", valueType = Integer.class, description = "assetID"),
                    @Key(name = "initNumber", valueType = BigInteger.class, description = "Initial asset quantity"),
                    @Key(name = "chainAssetAmount", valueType = BigInteger.class, description = "Number of existing assets")
            })
    )
    public Response getCirculateChainAsset(Map params) {
        try {
            int circulateChainId = Integer.valueOf(params.get("circulateChainId").toString());
            int assetChainId = Integer.valueOf(params.get("assetChainId").toString());
            int assetId = Integer.valueOf(params.get("assetId").toString());
            ChainAsset chainAsset = txCirculateService.getCirculateChainAsset(circulateChainId, assetChainId, assetId);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("circulateChainId", circulateChainId);
            resultMap.put("assetChainId", assetChainId);
            resultMap.put("assetId", assetId);
            if (null != chainAsset) {
                resultMap.put("initNumber", chainAsset.getInitNumber());
                resultMap.put("chainAssetAmount", chainAsset.getInNumber().subtract(chainAsset.getOutNumber()));
                return success(resultMap);
            } else {
                return failed(CmErrorCode.ERROR_ASSET_NOT_EXIST);
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
        }
        return failed(CmErrorCode.SYS_UNKOWN_EXCEPTION);
    }


    /**
     * Cross chain circulation verification
     */
    @CmdAnnotation(cmd = RpcConstants.CMD_ASSET_CIRCULATE_VALIDATOR, version = 1.0,
            description = "Query asset information")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "Running ChainID,Value range[1-65535]"),
            @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "transactionHexvalue")
    })
    @ResponseData(description = "No specific return value, validation successful without errors")
    public Response assetCirculateValidator(Map params) {
        //extract From which chain turn Which chain, whether it is cross chain, chain How much is the total handling fee？
        try {
            String txHex = String.valueOf(params.get("tx"));
            Transaction tx = TxUtil.buildTxData(txHex);
            if (null == tx) {
                return failed(CmErrorCode.ERROR_TX_HEX);
            }
            List<CoinDataAssets> list = txCirculateService.getChainAssetList(tx.getCoinData());
            CoinDataAssets fromCoinDataAssets = list.get(0);
            CoinDataAssets toCoinDataAssets = list.get(1);
            int fromChainId = fromCoinDataAssets.getChainId();
            int toChainId = toCoinDataAssets.getChainId();
            Map<String, BigInteger> fromAssetMap = fromCoinDataAssets.getAssetsMap();
            Map<String, BigInteger> toAssetMap = toCoinDataAssets.getAssetsMap();
            ChainEventResult chainEventResult = validateService.assetCirculateValidator(fromChainId, toChainId, fromAssetMap, toAssetMap);
            if (chainEventResult.isSuccess()) {
                Map<String, Boolean> resultMap = new HashMap<>();
                resultMap.put("value", true);
                return success(resultMap);
            } else {
                LoggerUtil.COMMON_LOG.error("--------assetCirculateValidator error:" + chainEventResult.getErrorCode().getCode());
                return failed(chainEventResult.getErrorCode());
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
        }
        return failed(CmErrorCode.SYS_UNKOWN_EXCEPTION);
    }

    /**
     * Cross chain circulation submission
     */
    @CmdAnnotation(cmd = RpcConstants.CMD_ASSET_CIRCULATE_COMMIT, version = 1.0,
            description = "Query asset information")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Running ChainID,Value range[1-65535]"),
            @Parameter(parameterName = "txList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "transactionHexValue List"),
            @Parameter(parameterName = "blockHeader", requestType = @TypeDescriptor(value = String.class), parameterDes = "Block headHexvalue")
    })
    @ResponseData(description = "No specific return value, submit successfully without errors")
    public Response assetCirculateCommit(Map params) {
        //AChain transferBChain assetsXquantityN ;AchainXAsset reductionN, Bchain Xasset increaseN.
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"));
            ObjectUtils.canNotEmpty(params.get("blockHeader"));
            Integer chainId = (Integer) params.get("chainId");
            List<String> txHexList = (List) params.get("txList");
            String blockHeaderStr = (String) params.get("blockHeader");
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.parse(RPCUtil.decode(blockHeaderStr), 0);
            long commitHeight = blockHeader.getHeight();
            List<Transaction> txList = new ArrayList<>();
            Response parseResponse = parseTxs(txHexList, txList);
            if (!parseResponse.isSuccess()) {
                return parseResponse;
            }
            /*begin bak datas*/
            cacheDataService.bakBlockTxs(chainId, commitHeight, txList, true);
            /*end bak datas*/
            /*begin bak height*/
            cacheDataService.beginBakBlockHeight(chainId, commitHeight);
            /*end bak height*/
            try {
                txCirculateService.circulateCommit(txList);
                /*begin bak height*/
                cacheDataService.endBakBlockHeight(chainId, commitHeight);
                /*end bak height*/
            } catch (Exception e) {
                LoggerUtil.logger().error(e);
                //Performing a rollback
                cacheDataService.rollBlockTxs(chainId, commitHeight);
                return failed(e.getMessage());
            }

        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(e.getMessage());
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", true);
        return success(resultMap);
    }

    /**
     * Cross chain circulation rollback
     */
    @CmdAnnotation(cmd = RpcConstants.CMD_ASSET_CIRCULATE_ROLLBACK, version = 1.0,
            description = "Query asset information")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Running ChainID,Value range[1-65535]"),
            @Parameter(parameterName = "txList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "transactionHexValue List"),
            @Parameter(parameterName = "blockHeader", requestType = @TypeDescriptor(value = String.class), parameterDes = "Block headHexvalue")
    })
    @ResponseData(description = "No specific return value, validation successful without errors")

    public Response assetCirculateRollBack(Map params) {
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", true);
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"));
            ObjectUtils.canNotEmpty(params.get("blockHeader"));
            Integer chainId = (Integer) params.get("chainId");
            List<String> txHexList = (List) params.get("txList");
            String blockHeaderStr = (String) params.get("blockHeader");
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.parse(RPCUtil.decode(blockHeaderStr), 0);
            long commitHeight = blockHeader.getHeight();
            List<Transaction> txList = new ArrayList<>();
            Response parseResponse = parseTxs(txHexList, txList);
            if (!parseResponse.isSuccess()) {
                return parseResponse;
            }
            //Roll back height first
            CacheDatas circulateTxDatas = cacheDataService.getCacheDatas(commitHeight - 1);
            if (null == circulateTxDatas) {
                LoggerUtil.logger().info("chain module height ={} bak datas is null,maybe had rolled", commitHeight);
                return success(resultMap);
            }
            //Performing data rollback
            cacheDataService.rollBlockTxs(chainId, commitHeight);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
        return success(resultMap);
    }

    @CmdAnnotation(cmd = RpcConstants.CMD_UPDATE_CHAIN_ASSET, version = 1.0,
            description = "Query and update circulating asset information")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Asset ChainID,Value range[1-65535]"),
            @Parameter(parameterName = "assets", requestType = @TypeDescriptor(value = List.class, collectionElement = Map.class, mapKeys = {
                    @Key(name = "assetId", valueType = Integer.class, description = "assetid"),
                    @Key(name = "availableAmount", valueType = BigInteger.class, description = "Available amount"),
                    @Key(name = "freeze", valueType = BigInteger.class, description = "Freeze amount"),
            }), parameterDes = "assetidlist")
    })
    @ResponseData(description = "No specific return value, validation successful without errors")
    public Response updateChainAsset(Map params) {
        List<Map<String, Object>> assets = new ArrayList<>();
        int chainId = 0;
        try {
            LoggerUtil.logger().debug("updateChainAsset json={}", JSONUtils.obj2json(params));
            chainId = Integer.valueOf(params.get("chainId").toString());
            assets = (List) params.get("assets");
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
            return failed(CmErrorCode.ERROR_PARAMETER);
        }
        List<ChainAssetTotalCirculate> chainAssetTotalCirculates = new ArrayList<>();
        for (Map<String, Object> asset : assets) {
            ChainAssetTotalCirculate chainAssetTotalCirculate = new ChainAssetTotalCirculate();
            chainAssetTotalCirculate.setAssetId(Integer.valueOf(asset.get("assetId").toString()));
            chainAssetTotalCirculate.setChainId(chainId);
            chainAssetTotalCirculate.setAvailableAmount(new BigInteger(asset.get("availableAmount").toString()));
            chainAssetTotalCirculate.setFreeze(new BigInteger(asset.get("freeze").toString()));
            chainAssetTotalCirculates.add(chainAssetTotalCirculate);
        }
        messageService.recChainIssuingAssets(chainId, chainAssetTotalCirculates);
        return success();
    }
}
