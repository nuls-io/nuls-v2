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
package io.nuls.chain.rpc.cmd;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.chain.info.CmErrorCode;
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
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.RPCUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 进行资产跨连交易流通的处理
 *
 * @author lan
 * @date 2019/02/21
 **/
@Component
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
     * 查询链上资产
     */
    @CmdAnnotation(cmd = "cm_getCirculateChainAsset", version = 1.0, description = "getCirculateChainAsset")
    @Parameter(parameterName = "circulateChainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "assetChainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "assetId", parameterType = "int", parameterValidRange = "[1,65535]")
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
     * 跨链流通校验
     */
    @CmdAnnotation(cmd = "cm_assetCirculateValidator", version = 1.0, description = "assetCirculateValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response assetCirculateValidator(Map params) {
        //提取 从哪条链 转 哪条链，是否是跨链，链 手续费共多少？
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
                return failed(chainEventResult.getErrorCode());
            }
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
        }
        return failed(CmErrorCode.SYS_UNKOWN_EXCEPTION);
    }

    /**
     * 跨链流通提交
     */
    @CmdAnnotation(cmd = "cm_assetCirculateCommit", version = 1.0, description = "assetCirculateCommit")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "txList", parameterType = "array")
    @Parameter(parameterName = "blockHeader", parameterType = "String")
    public Response assetCirculateCommit(Map params) {
        //A链转B链资产X，数量N ;A链X资产减少N, B链 X资产 增加N。
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
                //进行回滚
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
     * 跨链流通回滚
     */
    @CmdAnnotation(cmd = "cm_assetCirculateRollBack", version = 1.0, description = "assetCirculateRollBack")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "txList", parameterType = "array")
    @Parameter(parameterName = "blockHeader", parameterType = "array")
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
            //高度先回滚
            CacheDatas circulateTxDatas = cacheDataService.getCacheDatas(commitHeight - 1);
            if (null == circulateTxDatas) {
                LoggerUtil.logger().info("chain module height ={} bak datas is null,maybe had rolled", commitHeight);
                return success(resultMap);
            }
            //进行数据回滚
            cacheDataService.rollBlockTxs(chainId, commitHeight);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
        return success(resultMap);
    }


    @CmdAnnotation(cmd = "updateChainAsset", version = 1.0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "assets", parameterType = "Array")
    public Response updateChainAsset(Map params) {
        List<Map<String, Object>> assets = new ArrayList<>();
        int chainId = 0;
        try {
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
            chainAssetTotalCirculate.setFreeze(new BigInteger(asset.get("availableAmount").toString()));
            chainAssetTotalCirculates.add(chainAssetTotalCirculate);
        }
        messageService.recChainIssuingAssets(chainId, chainAssetTotalCirculates);
        return success();
    }
}
