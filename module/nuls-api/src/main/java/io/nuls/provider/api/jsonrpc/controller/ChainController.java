package io.nuls.provider.api.jsonrpc.controller;


import io.nuls.base.api.provider.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.rpc.model.Key;
import io.nuls.core.rpc.model.ResponseData;
import io.nuls.core.rpc.model.TypeDescriptor;
import io.nuls.provider.api.config.Config;
import io.nuls.provider.api.config.Context;
import io.nuls.provider.model.jsonrpc.RpcResult;
import io.nuls.provider.rpctools.BlockTools;
import io.nuls.provider.utils.ResultUtil;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;
import io.nuls.v2.model.annotation.ApiType;

import java.util.List;
import java.util.Map;

@Controller
@Api(type = ApiType.JSONRPC)
public class ChainController {

    @Autowired
    private Config config;
    @Autowired
    BlockTools blockTools;

    @RpcMethod("info")
    @ApiOperation(description = "获取本链相关信息,其中共识资产为本链创建共识节点交易和创建委托共识交易时，需要用到的资产", order = 001)
    @ResponseData(name = "返回值", description = "返回本链信息", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "chainId", description = "本链的ID"),
            @Key(name = "assetId", description = "本链默认主资产的ID"),
            @Key(name = "inflationAmount", description = "本链默认主资产的初始数量"),
            @Key(name = "agentChainId", description = "本链共识资产的链ID"),
            @Key(name = "agentAssetId", description = "本链共识资产的ID"),
            @Key(name = "addressPrefix", description = "本链地址前缀"),
            @Key(name = "symbol", description = "本链主资产符号")
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
     * 获取资产信息
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

}
