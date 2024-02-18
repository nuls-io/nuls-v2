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

import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.ledger.constant.CmdConstant;
import io.nuls.ledger.service.AssetRegMngService;
import io.nuls.ledger.utils.LoggerUtil;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Asset registration and management interface
 *
 * @author lanjinsheng .
 * @date 2019/10/22
 */
@Component
@NulsCoresCmd(module = ModuleE.LG)
public class AssetsRegCmd extends BaseLedgerCmd {
    @Autowired
    NulsCoresConfig ledgerConfig;
    @Autowired
    AssetRegMngService assetRegMngService;


    /**
     * View registered asset information within the chain
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_REG_INFO, version = 1.0,
            description = "View registered asset information within the chain")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Run ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "assetType", requestType = @TypeDescriptor(value = int.class), parameterDes = "Asset type")

    })
    @ResponseData(name = "Return value", description = "Return alistobject",
            responseType = @TypeDescriptor(value = List.class, collectionElement = Map.class, mapKeys = {
                    @Key(name = "assetId", valueType = int.class, description = "assetid"),
                    @Key(name = "assetType", valueType = int.class, description = "Asset type"),
                    @Key(name = "assetOwnerAddress", valueType = String.class, description = "Address of asset owner"),
                    @Key(name = "initNumber", valueType = BigInteger.class, description = "Asset initialization value"),
                    @Key(name = "decimalPlace", valueType = int.class, description = "Decimal Division"),
                    @Key(name = "assetName", valueType = String.class, description = "Asset Name"),
                    @Key(name = "assetSymbol", valueType = String.class, description = "Asset symbols"),
                    @Key(name = "txHash", valueType = String.class, description = "transactionhashvalue")
            })
    )
    public Response getAssetRegInfo(Map params) {
        Map<String, Object> rtMap = new HashMap<>(1);
        try {
            if (null == params.get("assetType")) {
                params.put("assetType", "0");
            }
            List<Map<String, Object>> assets = assetRegMngService.getLedgerRegAssets(Integer.valueOf(params.get("chainId").toString()), Integer.valueOf(params.get("assetType").toString()));
            rtMap.put("assets", assets);
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }

    /**
     * View registered asset information within the chain-Through assetsid
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_CHAIN_ASSET_REG_INFO_BY_ASSETID, version = 1.0,
            description = "Through assetsidView registered asset information within the chain")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Run ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = String.class), parameterValidRange = "[1-65535]", parameterDes = "assetid")

    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "assetId", valueType = int.class, description = "assetid"),
                    @Key(name = "assetType", valueType = int.class, description = "Asset type"),
                    @Key(name = "assetOwnerAddress", valueType = String.class, description = "Address of asset owner"),
                    @Key(name = "initNumber", valueType = BigInteger.class, description = "Asset initialization value"),
                    @Key(name = "decimalPlace", valueType = int.class, description = "Decimal Division"),
                    @Key(name = "assetName", valueType = String.class, description = "Asset Name"),
                    @Key(name = "assetSymbol", valueType = String.class, description = "Asset symbols"),
                    @Key(name = "txHash", valueType = String.class, description = "transactionhashvalue")
            })
    )
    public Response getAssetRegInfoByAssetId(Map params) {
        Map<String, Object> rtMap = new HashMap<>(1);
        try {
            rtMap = assetRegMngService.getLedgerRegAsset(Integer.valueOf(params.get("chainId").toString()), Integer.valueOf(params.get("assetId").toString()));
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return failed(e.getMessage());
        }
        return success(rtMap);
    }
}
