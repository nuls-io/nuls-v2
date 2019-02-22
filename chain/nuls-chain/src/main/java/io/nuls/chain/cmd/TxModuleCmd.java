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
import io.nuls.chain.info.ChainTxConstants;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.info.RpcConstants;
import io.nuls.chain.model.dto.ChainEventResult;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.chain.model.po.BlockHeight;
import io.nuls.chain.model.po.CacheDatas;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.CacheDataService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.ValidateService;
import io.nuls.chain.util.TxUtil;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.chain.util.LoggerUtil.Log;

/**
 * @author lan
 * @date 2018/11/22
 */
@Component
public class TxModuleCmd extends BaseChainCmd {
    @Autowired
    private ChainService chainService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private CacheDataService cacheDataService;
    @Autowired
    private ValidateService validateService;
    /**
     * chainModuleTxValidate
     * 批量校验
     */
    @CmdAnnotation(cmd = RpcConstants.TX_MODULE_VALIDATE_CMD_VALUE, version = 1.0,
            description = "chainModuleTxValidate")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "txHexs", parameterType = "array")
    public Response chainModuleTxValidate(Map params) {
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"));
            ObjectUtils.canNotEmpty(params.get("txHexList"));
            ObjectUtils.canNotEmpty(params.get("blockHeaderDigest"));
            Integer chainId = (Integer) params.get("chainId");
            List<String> txHexList = (List) params.get("txHexList");
            List<Transaction> txList = new ArrayList<>();
            Response parseResponse = parseTxs(txHexList, txList);
            if (!parseResponse.isSuccess()) {
                return parseResponse;
            }
            //1获取交易类型
            //2进入不同验证器里处理
            //3封装失败交易返回
            Map<String, Integer> chainMap = new HashMap<>();
            Map<String, Integer> assetMap = new HashMap<>();
            BlockChain blockChain = null;
            Asset asset = null;
            ChainEventResult chainEventResult = ChainEventResult.getResultSuccess();
            for (Transaction tx : txList) {
                switch (tx.getType()) {
                    case ChainTxConstants.TX_TYPE_REGISTER_CHAIN_AND_ASSET:
                        blockChain = TxUtil.buildChainWithTxData(tx, false);
                        asset = TxUtil.buildAssetWithTxChain(tx);
                        String assetKey = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
                        chainMap.put(String.valueOf(blockChain.getChainId()), 1);
                        assetMap.put(assetKey, 1);
                        chainEventResult = validateService.batchChainRegValidator(blockChain, asset, chainMap, assetMap);
                        if (chainEventResult.isSuccess()) {
                            Log.info("txHash = {},chainId={} reg batchValidate success!", tx.getHash().toString(), blockChain.getChainId());
                        } else {
                            Log.info("txHash = {},chainId={} reg batchValidate fail!", tx.getHash().toString(), blockChain.getChainId());
                            return failed(chainEventResult.getErrorCode());
                        }
                        break;
                    case ChainTxConstants.TX_TYPE_DESTROY_ASSET_AND_CHAIN:
                        blockChain = TxUtil.buildChainWithTxData(tx, true);
                        chainEventResult = validateService.chainDisableValidator(blockChain);
                        if (chainEventResult.isSuccess()) {
                            Log.info("txHash = {},chainId={} destroy batchValidate success!", tx.getHash().toString(), blockChain.getChainId());
                        } else {
                            Log.info("txHash = {},chainId={} destroy batchValidate fail!", tx.getHash().toString(), blockChain.getChainId());
                            return failed(chainEventResult.getErrorCode());
                        }
                        break;

                    case ChainTxConstants.TX_TYPE_ADD_ASSET_TO_CHAIN:
                        asset = TxUtil.buildAssetWithTxChain(tx);
                        String assetKey2 = CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId());
                        assetMap.put(assetKey2, 1);
                        chainEventResult = validateService.batchAssetRegValidator(asset, assetMap);
                        if (chainEventResult.isSuccess()) {
                            Log.info("txHash = {},assetKey={} reg batchValidate success!", tx.getHash().toString(), assetKey2);
                        } else {
                            Log.info("txHash = {},assetKey={} reg batchValidate fail!", tx.getHash().toString(), assetKey2);
                            return failed(chainEventResult.getErrorCode());
                        }
                        break;
                    case ChainTxConstants.TX_TYPE_REMOVE_ASSET_FROM_CHAIN:
                        asset = TxUtil.buildAssetWithTxChain(tx);
                        chainEventResult = validateService.assetDisableValidator(asset);
                        if (chainEventResult.isSuccess()) {
                            Log.info("txHash = {},assetKey={} disable batchValidate success!", tx.getHash().toString(), CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
                        } else {
                            Log.info("txHash = {},assetKey={} disable batchValidate fail!", tx.getHash().toString(), CmRuntimeInfo.getAssetKey(asset.getChainId(), asset.getAssetId()));
                            return failed(chainEventResult.getErrorCode());
                        }
                        break;
                    default:
                        break;
                }
            }
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    /**
     * moduleTxsRollBack
     * 回滚
     */
    @CmdAnnotation(cmd = RpcConstants.TX_ROLLBACK_CMD_VALUE, version = 1.0,
            description = "moduleTxsRollBack")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "txHexList", parameterType = "array")
    @Parameter(parameterName = "blockHeaderDigest", parameterType = "array")
    public Response moduleTxsRollBack(Map params) {
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
            CacheDatas moduleTxDatas = cacheDataService.getCacheDatas(commitHeight);
            if(null == moduleTxDatas){
                Log.error("chain module height ={} bak datas is null",commitHeight);
                return failed("chain module height = "+commitHeight+" bak datas is null");
            }

            //通知远程调用回滚
            chainService.rpcBlockChainRollback(txList);
            //进行数据回滚
            cacheDataService.rollBlockTxs(chainId,commitHeight);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", true);
        return success(resultMap);
    }
    /**
     * moduleTxsCommit
     * 批量提交
     */
    @CmdAnnotation(cmd = RpcConstants.TX_COMMIT_CMD_VALUE, version = 1.0,
            description = "moduleTxsCommit")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "txHexList", parameterType = "array")
    @Parameter(parameterName = "blockHeaderDigest", parameterType = "array")
    public Response moduleTxsCommit(Map params) {
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"));
            ObjectUtils.canNotEmpty(params.get("txHexList"));
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
            cacheDataService.bakBlockTxs(chainId,dbHeight.getBlockHeight(),commitHeight,txList,false);
            /*end bak datas*/
            /*begin bak height*/
            cacheDataService.beginBakBlockHeight(chainId,commitHeight);
            /*end bak height*/
            BlockChain blockChain = null;
            Asset asset = null;
            try {
                for (Transaction tx : txList) {
                    switch (tx.getType()) {
                        case ChainTxConstants.TX_TYPE_REGISTER_CHAIN_AND_ASSET:
                            blockChain = TxUtil.buildChainWithTxData(tx, false);
                            asset = TxUtil.buildAssetWithTxChain(tx);
                            chainService.registerBlockChain(blockChain, asset);
                            //通知网络模块创建链
                            break;
                        case ChainTxConstants.TX_TYPE_DESTROY_ASSET_AND_CHAIN:
                            blockChain = TxUtil.buildChainWithTxData(tx, true);
                            chainService.destroyBlockChain(blockChain);
                            break;
                        case ChainTxConstants.TX_TYPE_ADD_ASSET_TO_CHAIN:
                            asset =TxUtil.buildAssetWithTxChain(tx);
                            assetService.registerAsset(asset);
                            break;
                        case ChainTxConstants.TX_TYPE_REMOVE_ASSET_FROM_CHAIN:
                            asset = TxUtil.buildAssetWithTxChain(tx);
                            assetService.deleteAsset(asset);
                            break;
                        default:
                            break;
                    }
                }
                Log.debug("moduleTxsCommit end");
                /*begin bak height*/
                cacheDataService.endBakBlockHeight(chainId,commitHeight);
                /*end bak height*/
            } catch (Exception e) {
                Log.error(e);
                //通知远程调用回滚
                chainService.rpcBlockChainRollback(txList);
                //进行回滚
                cacheDataService.rollBlockTxs(chainId,commitHeight);
                return failed(e.getMessage());
            }

        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", true);
        return success(resultMap);
    }


}
