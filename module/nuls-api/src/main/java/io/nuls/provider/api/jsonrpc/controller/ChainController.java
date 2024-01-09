package io.nuls.provider.api.jsonrpc.controller;


import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.crosschain.ChainManageProvider;
import io.nuls.base.api.provider.crosschain.facade.CrossAssetRegisterInfo;
import io.nuls.base.api.provider.crosschain.facade.GetCrossAssetInfoReq;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.rpc.model.*;
import io.nuls.provider.api.config.Config;
import io.nuls.provider.api.config.Context;
import io.nuls.provider.model.dto.ProgramMethod;
import io.nuls.provider.model.jsonrpc.RpcResult;
import io.nuls.provider.rpctools.BlockTools;
import io.nuls.provider.utils.ResultUtil;
import io.nuls.provider.utils.VerifyUtils;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;
import io.nuls.v2.model.annotation.ApiType;

import java.util.List;
import java.util.Map;

@Controller
@Api(type = ApiType.JSONRPC)
public class ChainController {

    ChainManageProvider chainManageProvider = ServiceManager.get(ChainManageProvider.class);
    @Autowired
    private Config config;
    @Autowired
    BlockTools blockTools;

    @RpcMethod("info")
    @ApiOperation(description = "Obtain information related to this chain,Among them, consensus assets are the assets required for creating consensus node transactions and creating delegated consensus transactions in this chain", order = 001)
    @ResponseData(name = "Return value", description = "Return this chain information", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "chainId", description = "This chain'sID"),
            @Key(name = "assetId", description = "This chain defaults to the main asset'sID"),
            @Key(name = "inflationAmount", description = "The initial quantity of the default main asset in this chain"),
            @Key(name = "agentChainId", description = "The chain of consensus assets in this chainID"),
            @Key(name = "agentAssetId", description = "The consensus assets of this chainID"),
            @Key(name = "addressPrefix", description = "Prefix for this chain address"),
            @Key(name = "symbol", description = "Main asset symbol of this chain")
    }))
    public RpcResult getInfo(List<Object> params) {
        Result<Map> result = blockTools.getInfo(config.getChainId());
        if (result.isSuccess()) {
            Map map = result.getData();
            map.put("chainId", config.getChainId());
            map.put("assetId", config.getAssetsId());
            map.put("addressPrefix", config.getAddressPrefix());
            map.put("symbol", config.getSymbol());
            map.remove("awardAssetId");
            map.remove("seedNodes");
        }
        return ResultUtil.getJsonRpcResult(result);
    }


    /**
     * Obtain asset information
     *
     * @param params
     * @return
     */
    @RpcMethod("assetsInfo")
    public RpcResult getAssetsInfo(List<Object> params) {
        if (Context.isRunCrossChain) {
            return RpcResult.success(Context.assetList);
        } else {
            return RpcResult.success(Context.defaultChain.getAssets());
        }
    }

    /**
     * Obtain parallel chain asset information
     *
     * @param params
     * @return
     */
    @RpcMethod("getCrossAssetInfo")
    @ApiOperation(description = "Obtain parallel chain asset information", order = 603)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "Asset ChainID"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterDes = "assetID"),
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = CrossAssetRegisterInfo.class))
    public RpcResult getCrossAssetInfo(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId, assetId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            assetId = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[assetId] is invalid");
        }
        Result<CrossAssetRegisterInfo> result = chainManageProvider.getCrossAssetInfo(new GetCrossAssetInfoReq(chainId,assetId));
        return ResultUtil.getJsonRpcResult(result);
    }

}
