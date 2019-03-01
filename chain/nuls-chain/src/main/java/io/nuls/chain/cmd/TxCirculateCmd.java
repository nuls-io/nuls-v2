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
package io.nuls.chain.cmd;

import io.nuls.base.data.BlockHeaderDigest;
import io.nuls.base.data.Transaction;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.model.dto.ChainEventResult;
import io.nuls.chain.model.dto.CoinDataAssets;
import io.nuls.chain.model.po.BlockHeight;
import io.nuls.chain.model.po.CacheDatas;
import io.nuls.chain.service.*;
import io.nuls.chain.util.LoggerUtil;
import io.nuls.chain.util.TxUtil;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.data.ObjectUtils;
import io.nuls.tools.log.Log;

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


    /**
     * 跨链流通校验
     */
    @CmdAnnotation(cmd = "cm_assetCirculateValidator", version = 1.0, description = "assetCirculateValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "txHex", parameterType = "String")
    public Response assetCirculateValidator(Map params) {
        //提取 从哪条链 转 哪条链，是否是跨链，链 手续费共多少？
        try {
            String txHex = String.valueOf(params.get("txHex"));
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
            e.printStackTrace();
        }
        return failed(CmErrorCode.UNKOWN_ERROR);
    }

    /**
     * 跨链流通提交
     */
    @CmdAnnotation(cmd = "cm_assetCirculateCommit", version = 1.0, description = "assetCirculateCommit")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "txHexList", parameterType = "array")
    @Parameter(parameterName = "blockHeaderDigest", parameterType = "array")
    public Response assetCirculateCommit(Map params) {
        //A链转B链资产X，数量N ;A链X资产减少N, B链 X资产 增加N。
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"));
            ObjectUtils.canNotEmpty(params.get("blockHeaderDigest"));
            Integer chainId = (Integer) params.get("chainId");
            List<String> txHexList = (List) params.get("txHexList");
            BlockHeaderDigest blockHeaderDigest = (BlockHeaderDigest) params.get("blockHeaderDigest");
            long commitHeight = blockHeaderDigest.getHeight();
            List<Transaction> txList = new ArrayList<>();
            Response parseResponse = parseTxs(txHexList, txList);
            if (!parseResponse.isSuccess()) {
                return parseResponse;
            }
            /*begin bak datas*/
            BlockHeight dbHeight = cacheDataService.getBlockHeight(chainId);
            cacheDataService.bakBlockTxs(chainId, dbHeight.getBlockHeight(), commitHeight, txList, true);
            /*end bak datas*/
            /*begin bak height*/
            cacheDataService.beginBakBlockHeight(chainId, commitHeight);
            /*end bak height*/
            try {
                txCirculateService.circulateCommit(txList);
                LoggerUtil.Log.debug("moduleTxsCommit end");
                /*begin bak height*/
                cacheDataService.endBakBlockHeight(chainId, commitHeight);
                /*end bak height*/
            } catch (Exception e) {
                LoggerUtil.Log.error(e);
                //进行回滚
                cacheDataService.rollBlockTxs(chainId, commitHeight);
                return failed(e.getMessage());
            }

        } catch (Exception e) {
            LoggerUtil.Log.error(e);
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
    @Parameter(parameterName = "txHexList", parameterType = "array")
    @Parameter(parameterName = "blockHeaderDigest", parameterType = "array")
    public Response assetCirculateRollBack(Map params) {
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"));
            ObjectUtils.canNotEmpty(params.get("blockHeaderDigest"));
            Integer chainId = (Integer) params.get("chainId");
            List<String> txHexList = (List) params.get("txHexList");
            BlockHeaderDigest blockHeaderDigest = (BlockHeaderDigest) params.get("blockHeaderDigest");
            long commitHeight = blockHeaderDigest.getHeight();
            List<Transaction> txList = new ArrayList<>();
            Response parseResponse = parseTxs(txHexList, txList);
            if (!parseResponse.isSuccess()) {
                return parseResponse;
            }
            //高度先回滚
            CacheDatas circulateTxDatas = cacheDataService.getCacheDatas(commitHeight);
            if (null == circulateTxDatas) {
                BlockHeight blockHeight = cacheDataService.getBlockHeight(chainId);
                //这里存在该高度 可能在TxCirculateCmd中已经回滚过了
                if (blockHeight.getLatestRollHeight() == commitHeight) {
                    LoggerUtil.Log.debug("chain module height ={} bak datas is null,maybe had rolled", commitHeight);
                    return success();
                } else {
                    LoggerUtil.Log.error("chain module height ={} bak datas is null", commitHeight);
                    return failed("chain module height = " + commitHeight + " bak datas is null");
                }
            }
            //进行数据回滚
            cacheDataService.rollBlockTxs(chainId, commitHeight);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", true);
        return success(resultMap);
    }
}
