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
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
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
 * Asset registration and management interface
 *
 * @author lanjinsheng .
 * @date 2019/10/22
 */
@Component
@NulsCoresCmd(module = ModuleE.LG)
public class AssetsRegContractCmd extends BaseLedgerCmd {
    @Autowired
    NulsCoresConfig ledgerConfig;
    @Autowired
    CallRpcService rpcService;
    @Autowired
    AccountStateService accountStateService;
    @Autowired
    AssetRegMngService assetRegMngService;


    /**
     * In chain asset contract registration interface
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_CONTRACT_REG, version = 1.0,
            description = "In chain asset contract registration interface")
    @Parameters(value = {
            @Parameter(parameterName = "assetName", requestType = @TypeDescriptor(value = String.class), parameterDes = "Asset Name: large、Lowercase letters、number、Underline（The underline cannot be at both ends）1~20byte"),
            @Parameter(parameterName = "initNumber", requestType = @TypeDescriptor(value = BigInteger.class), parameterDes = "Initial value of assets"),
            @Parameter(parameterName = "decimalPlace", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-18]", parameterDes = "The minimum number of split digits for assets"),
            @Parameter(parameterName = "assetSymbol", requestType = @TypeDescriptor(value = String.class), parameterDes = "Asset unit symbol: large、Lowercase letters、number、Underline（The underline cannot be at both ends）1~20byte"),
            @Parameter(parameterName = "contractAddress", requestType = @TypeDescriptor(value = String.class), parameterDes = "New Asset Smart Contract Address"),
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "chainId", valueType = int.class, description = "chainid"),
                    @Key(name = "assetId", valueType = int.class, description = "assetid")
            })
    )
    public Response chainAssetContractReg(Map params) {
        Map<String, Object> rtMap = new HashMap<>(3);
        try {
            LoggerUtil.COMMON_LOG.debug("params={}", JSONUtils.obj2json(params));
            /* assembleAsset (Asset object) */
            params.put("chainId", ledgerConfig.getChainId());
            params.put("address", params.get("contractAddress"));
            LedgerAsset asset = new LedgerAsset();
            asset.map2pojo(params, LedgerConstant.CONTRACT_ASSET_TYPE);
            int assetId = assetRegMngService.registerContractAsset(asset.getChainId(), asset);
            rtMap.put("assetId", assetId);
            rtMap.put("chainId", asset.getChainId());
            LoggerUtil.COMMON_LOG.debug("return={}", JSONUtils.obj2json(rtMap));
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }

    /**
     * In chain asset contract registration rollback
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_CONTRACT_ROLL_BACK, version = 1.0,
            description = "In chain asset contract registration interface")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "chainId"),
            @Parameter(parameterName = "contractAddress", requestType = @TypeDescriptor(value = String.class), parameterDes = "New Asset Smart Contract Address"),
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "value", valueType = boolean.class, description = "successtrue,failfalse")
            })
    )
    public Response chainAssetContractRollBack(Map params) {
        Map<String, Object> rtMap = new HashMap<>(1);
        try {
            /* assembleAsset (Asset object) */
            assetRegMngService.rollBackContractAsset(ledgerConfig.getChainId(), params.get("contractAddress").toString());
            rtMap.put("value", true);
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }

    /**
     * Asset contract address inquiry
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_CONTRACT_ADDRESS, version = 1.0,
            description = "Asset contract address inquiry")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "chainId"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "assetid"),
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "contractAddress", valueType = String.class, description = "Contract address")
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
     * Asset contract address inquiry
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_CONTRACT_ASSETID, version = 1.0,
            description = "Asset contract assetsIDquery")
    @Parameters(value = {
            @Parameter(parameterName = "contractAddress", requestType = @TypeDescriptor(value = String.class), parameterDes = "Contract address")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "chainId", valueType = int.class, description = "chainId"),
                    @Key(name = "assetId", valueType = int.class, description = "assetId")
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

    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_CONTRACT, version = 1.0,
            description = "Contract asset inquiry")
    @Parameters(value = {
            @Parameter(parameterName = "contractAddress", requestType = @TypeDescriptor(value = String.class), parameterDes = "Contract address")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "assetId", valueType = int.class, description = "assetid"),
                    @Key(name = "assetType", valueType = int.class, description = "Asset type"),
                    @Key(name = "assetOwnerAddress", valueType = String.class, description = "Address of asset owner"),
                    @Key(name = "initNumber", valueType = BigInteger.class, description = "Asset initialization value"),
                    @Key(name = "decimalPlace", valueType = int.class, description = "Decimal Division"),
                    @Key(name = "assetName", valueType = String.class, description = "Asset Name"),
                    @Key(name = "assetSymbol", valueType = String.class, description = "Asset symbols")
            })
    )
    public Response getAssetRegInfoByAddress(Map params) {
        Map<String, Object> rtMap = new HashMap<>(1);
        try {
            String address = params.get("contractAddress").toString();
            int chainId = AddressTool.getChainIdByAddress(address);
            int assetId = assetRegMngService.getRegAssetId(chainId, address);
            rtMap = assetRegMngService.getLedgerRegAsset(chainId, assetId);
            if (null != rtMap) {
                rtMap.remove("txHash");
            }
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }
}
