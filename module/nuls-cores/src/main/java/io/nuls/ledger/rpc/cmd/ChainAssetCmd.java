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
 * @description 查询链资产接口
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
            description = "查询链下指定资产集合的金额信息")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "运行的链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetIds", requestType = @TypeDescriptor(value = String.class), parameterDes = "资产id,逗号分隔")
    })
    @ResponseData(name = "返回值", description = "返回一个List对象",
            responseType = @TypeDescriptor(value = List.class, collectionElement = Map.class, mapKeys = {
                    @Key(name = "assetId", valueType = Integer.class, description = "资产id"),
                    @Key(name = "availableAmount", valueType = BigInteger.class, description = "可用金额"),
                    @Key(name = "freeze", valueType = BigInteger.class, description = "冻结金额")
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
            description = "查询链下指定资产集合的金额信息")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "运行的链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetChainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "资产链id"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterDes = "资产id")
    })
    @ResponseData(name = "返回值", description = "返回一个List对象",
            responseType = @TypeDescriptor(value = List.class, collectionElement = Map.class, mapKeys = {
                    @Key(name = "assetId", valueType = Integer.class, description = "资产id"),
                    @Key(name = "availableAmount", valueType = BigInteger.class, description = "可用金额"),
                    @Key(name = "freeze", valueType = BigInteger.class, description = "冻结金额")
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
     * 查看所有登记资产信息
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_GET_ALL_ASSET, version = 1.0,
            description = "查看所有登记资产信息")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "运行链Id,取值区间[1-65535]")
    })
    @ResponseData(name = "返回值", description = "返回一个list对象",
            responseType = @TypeDescriptor(value = List.class, collectionElement = Map.class, mapKeys = {
                    @Key(name = "assetChainId", valueType = int.class, description = "资产链id"),
                    @Key(name = "assetId", valueType = int.class, description = "资产id"),
                    @Key(name = "assetType", valueType = int.class, description = "资产类型 [1-链内普通资产 2-链内合约资产 3-平行链资产 4-异构链资产 5-链内普通资产绑定异构链资产 6-平行链资产绑定异构链资产 7-链内普通资产绑定多异构链资产 8-平行链资产绑定多异构链资产 9-异构链资产绑定多异构链资产]"),
                    @Key(name = "assetAddress", valueType = String.class, description = "资产地址"),
                    @Key(name = "initNumber", valueType = BigInteger.class, description = "资产初始化值"),
                    @Key(name = "decimalPlace", valueType = int.class, description = "小数点分割位数"),
                    @Key(name = "assetName", valueType = String.class, description = "资产名"),
                    @Key(name = "assetSymbol", valueType = String.class, description = "资产符号")
            })
    )
    public Response getAllCrossChainAssets(Map params) {
        Map<String, Object> rtMap = new HashMap<>(2);
        try {
            int chainId = Integer.parseInt(params.get("chainId").toString());
            // 获取所有注册的链内资产
            List<LedgerAsset> localAssetList = assetRegMngRepository.getAllRegLedgerAssets(chainId);
            List<Map<String, Object>> assets = localAssetList.stream().map(asset -> asset.toMap()).collect(Collectors.toList());
            assets.add(ledgerChainManager.getLocalChainDefaultAsset());
            // 获取所有登记的跨链资产
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
