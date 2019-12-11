/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.rpc.cmd;

import io.nuls.base.basic.AddressTool;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.ledger.config.LedgerConfig;
import io.nuls.ledger.constant.CmdConstant;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.po.LedgerAsset;
import io.nuls.ledger.rpc.call.CallRpcService;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.AssetRegMngService;
import io.nuls.ledger.utils.LoggerUtil;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * 资产登记与管理接口
 *
 * @author lanjinsheng .
 * @date 2019/10/22
 */
@Component
public class AssetsRegContractCmd extends BaseLedgerCmd {
    @Autowired
    LedgerConfig ledgerConfig;
    @Autowired
    CallRpcService rpcService;
    @Autowired
    AccountStateService accountStateService;
    @Autowired
    AssetRegMngService assetRegMngService;


    /**
     * 链内资产合约登记接口
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_CONTRACT_REG, version = 1.0,
            description = "链内资产合约登记接口")
    @Parameters(value = {
            @Parameter(parameterName = "assetName", requestType = @TypeDescriptor(value = String.class), parameterDes = "资产名称: 大、小写字母、数字、下划线（下划线不能在两端）1~20字节"),
            @Parameter(parameterName = "initNumber", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "资产初始值"),
            @Parameter(parameterName = "decimalPlace", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-18]", parameterDes = "资产最小分割位数"),
            @Parameter(parameterName = "assetSymbol", requestType = @TypeDescriptor(value = String.class), parameterDes = "资产单位符号: 大、小写字母、数字、下划线（下划线不能在两端）1~20字节"),
            @Parameter(parameterName = "contractAddress", requestType = @TypeDescriptor(value = String.class), parameterDes = "新资产智能合约地址"),
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "chainId", valueType = int.class, description = "链id"),
                    @Key(name = "assetId", valueType = int.class, description = "资产id")
            })
    )
    public Response chainAssetContractReg(Map params) {
        Map<String, Object> rtMap = new HashMap<>(3);
        try {
            LoggerUtil.COMMON_LOG.debug("params={}",JSONUtils.obj2json(params));
            /* 组装Asset (Asset object) */
            params.put("chainId", ledgerConfig.getChainId());
            params.put("address", params.get("contractAddress"));
            LedgerAsset asset = new LedgerAsset();
            asset.map2pojo(params, LedgerConstant.CONTRACT_ASSET_TYPE);
            int assetId = assetRegMngService.registerContractAsset(asset.getChainId(), asset);
            rtMap.put("assetId", assetId);
            rtMap.put("chainId", asset.getChainId());
            LoggerUtil.COMMON_LOG.debug("return={}",JSONUtils.obj2json(rtMap));
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }

    /**
     * 链内资产合约登记回滚
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_CONTRACT_ROLL_BACK, version = 1.0,
            description = "链内资产合约登记接口")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "链Id"),
            @Parameter(parameterName = "contractAddress", requestType = @TypeDescriptor(value = String.class), parameterDes = "新资产智能合约地址"),
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "value", valueType = boolean.class, description = "成功true,失败false")
            })
    )
    public Response chainAssetContractRollBack(Map params) {
        Map<String, Object> rtMap = new HashMap<>(1);
        try {
            /* 组装Asset (Asset object) */
            assetRegMngService.rollBackContractAsset(ledgerConfig.getChainId(), params.get("contractAddress").toString());
            rtMap.put("value", true);
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }

    /**
     * 资产合约地址查询
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_CONTRACT_ADDRESS, version = 1.0,
            description = "资产合约地址查询")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "链Id"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "资产id"),
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "contractAddress", valueType = String.class, description = "合约地址")
            })
    )
    public Response getAssetContractAddress(Map params) {
        Map<String, Object> rtMap = new HashMap<>(1);
        try {
            String address = assetRegMngService.getRegAssetContractAddr(Integer.valueOf(params.get("chainId").toString()), Integer.valueOf(params.get("assetId").toString()));
            rtMap.put("contractAddress", address);
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }

    /**
     * 资产合约地址查询
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_CONTRACT_ASSETID, version = 1.0,
            description = "资产合约资产ID查询")
    @Parameters(value = {
            @Parameter(parameterName = "contractAddress", requestType = @TypeDescriptor(value = String.class), parameterDes = "合约地址")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "chainId", valueType = int.class, description = "链Id"),
                    @Key(name = "assetId", valueType = int.class, description = "资产Id")
            })
    )
    public Response getAssetContractAssetId(Map params) {
        Map<String, Object> rtMap = new HashMap<>(2);
        try {
            String address = params.get("contractAddress").toString();
            int chainId = AddressTool.getChainIdByAddress(address);
            int assetId = assetRegMngService.getRegAssetId(chainId, address);
            rtMap.put("chainId", chainId);
            rtMap.put("assetId", assetId);
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }
}
