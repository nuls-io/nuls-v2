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
package io.nuls.ledger.rpc.cmd;


import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.ledger.constant.CmdConstant;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.manager.LedgerChainManager;
import io.nuls.ledger.model.po.LedgerAsset;
import io.nuls.ledger.rpc.call.CallRpcService;
import io.nuls.ledger.service.ChainAssetsService;
import io.nuls.ledger.storage.AssetRegMngRepository;
import io.nuls.ledger.storage.CrossChainAssetRegMngRepository;
import io.nuls.ledger.utils.LoggerUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author lan
 * @description Query Chain Asset Interface
 * @date 2019/02/14
 **/
@Component
@NulsCoresCmd(module = ModuleE.LG)
public class ChainAssetCmd extends BaseLedgerCmd {
    @Autowired
    ChainAssetsService chainAssetsService;
    @Autowired
    AssetRegMngRepository assetRegMngRepository;
    @Autowired
    LedgerChainManager ledgerChainManager;
    @Autowired
    CallRpcService callRpcService;

    @CmdAnnotation(cmd = CmdConstant.CMD_GET_ASSETS_BY_ID, version = 1.0,
            description = "Query the amount information of a specified set of assets off the chain")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Running ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "assetIds", requestType = @TypeDescriptor(value = String.class), parameterDes = "assetid,Comma separated")
    })
    @ResponseData(name = "Return value", description = "Return aListobject",
            responseType = @TypeDescriptor(value = List.class, collectionElement = Map.class, mapKeys = {
                    @Key(name = "assetId", valueType = Integer.class, description = "assetid"),
                    @Key(name = "availableAmount", valueType = BigInteger.class, description = "Available amount"),
                    @Key(name = "freeze", valueType = BigInteger.class, description = "Freeze amount")
            })
    )
    public Response getAssetsById(Map params) {
        List<Map<String, Object>> rtAssetList = new ArrayList<>();
        Integer addressChainId = (Integer) params.get("chainId");
        Integer assetChainId = addressChainId;
        if (!chainHanlder(addressChainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        String assetIds = params.get("assetIds").toString();
        String[] assetIdList = assetIds.split(LedgerConstant.COMMA);
        for (String assetIdStr : assetIdList) {
            Map<String, Object> map = chainAssetsService.getAssetByChainAssetId(addressChainId, assetChainId, Integer.valueOf(assetIdStr));
            rtAssetList.add(map);
        }
        Map<String, Object> rtMap = new HashMap<>();
        rtMap.put("assets", rtAssetList);
        return success(rtMap);
    }


    @CmdAnnotation(cmd = CmdConstant.CMD_GET_ASSET_BY_ID, version = 1.0,
            description = "Query the amount information of a specified set of assets off the chain")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Running ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "assetChainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "Asset Chainid"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterDes = "assetid")
    })
    @ResponseData(name = "Return value", description = "Return aListobject",
            responseType = @TypeDescriptor(value = List.class, collectionElement = Map.class, mapKeys = {
                    @Key(name = "assetId", valueType = Integer.class, description = "assetid"),
                    @Key(name = "availableAmount", valueType = BigInteger.class, description = "Available amount"),
                    @Key(name = "freeze", valueType = BigInteger.class, description = "Freeze amount")
            })
    )
    public Response getAssetById(Map params) {
        int chainId = (Integer) params.get("chainId");
        int assetChainId = (Integer) params.get("assetChainId");
        int assetId = (Integer) params.get("assetId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        Map<String, Object> map = chainAssetsService.getAssetByChainAssetId(chainId, assetChainId, assetId);
        return success(map);
    }

    /**
     * View all registered asset information
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_GET_ALL_ASSET, version = 1.0,
            description = "View all registered asset information")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Run ChainId,Value range[1-65535]")
    })
    @ResponseData(name = "Return value", description = "Return alistobject",
            responseType = @TypeDescriptor(value = List.class, collectionElement = Map.class, mapKeys = {
                    @Key(name = "assetChainId", valueType = int.class, description = "Asset Chainid"),
                    @Key(name = "assetId", valueType = int.class, description = "assetid"),
                    @Key(name = "assetType", valueType = int.class, description = "Asset type [1-On chain ordinary assets 2-On chain contract assets 3-Parallel chain assets 4-Heterogeneous chain assets 5-On chain ordinary assets bound to heterogeneous chain assets 6-Parallel chain assets bound to heterogeneous chain assets 7-Binding ordinary assets within the chain to multiple heterogeneous chain assets 8-Binding Parallel Chain Assets to Multiple Heterogeneous Chain Assets 9-Binding heterogeneous chain assets to multiple heterogeneous chain assets]"),
                    @Key(name = "assetAddress", valueType = String.class, description = "Asset address"),
                    @Key(name = "initNumber", valueType = BigInteger.class, description = "Asset initialization value"),
                    @Key(name = "decimalPlace", valueType = int.class, description = "Decimal Division"),
                    @Key(name = "assetName", valueType = String.class, description = "Asset Name"),
                    @Key(name = "assetSymbol", valueType = String.class, description = "Asset symbols")
            })
    )
    public Response getAllCrossChainAssets(Map params) {
        Map<String, Object> rtMap = new HashMap<>(2);
        try {
            int chainId = Integer.parseInt(params.get("chainId").toString());
            // Obtain all registered in chain assets
            List<LedgerAsset> localAssetList = assetRegMngRepository.getAllRegLedgerAssets(chainId);
            List<Map<String, Object>> assets = localAssetList.stream().map(asset -> asset.toMap()).collect(Collectors.toList());
            assets.add(ledgerChainManager.getLocalChainDefaultAsset());
            // Obtain all registered cross chain assets
            List<Map<String, Object>> crossAssetList = callRpcService.getRegisteredChainInfoList(chainId);
            if (crossAssetList != null) {
                assets.addAll(crossAssetList);
            }

            rtMap.put("assets", assets);
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }
}
