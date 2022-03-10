package io.nuls.provider.api.jsonrpc.controller;

import io.nuls.base.api.provider.Result;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.provider.api.model.AssetInfo;
import io.nuls.provider.model.jsonrpc.RpcResult;
import io.nuls.provider.model.jsonrpc.RpcResultError;
import io.nuls.provider.rpctools.CrossChainTools;
import io.nuls.provider.rpctools.LegderTools;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiType;

import java.util.List;
import java.util.Map;

@Controller
@Api(type = ApiType.JSONRPC)
public class LegerController {


    @Autowired
    private LegderTools legderTools;
    @Autowired
    private CrossChainTools crossChainTools;

    @RpcMethod("getAllAsset")
    public RpcResult getAllAsset(List<Object> params) {
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }

        Result<Map> result = legderTools.getAllAsset(chainId);
        RpcResult rpcResult = new RpcResult();
        if (result.isFailed()) {
            return rpcResult.setError(new RpcResultError(result.getStatus(), result.getMessage(), null));
        }
        return rpcResult.setResult(result.getList());
    }

    @RpcMethod("getAllCrossAsset")
    public RpcResult getAllCrossAsset(List<Object> params) {
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }

        List<AssetInfo> assetInfos = crossChainTools.getRegisteredChainInfoList(chainId);
        RpcResult rpcResult = new RpcResult();
        if (assetInfos == null) {
            return rpcResult.setError(new RpcResultError(CommonCodeConstanst.DATA_NOT_FOUND.getCode(), null, null));
        }
        return rpcResult.setResult(assetInfos);
    }
}
