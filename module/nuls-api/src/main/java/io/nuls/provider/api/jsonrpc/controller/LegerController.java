package io.nuls.provider.api.jsonrpc.controller;

import io.nuls.base.api.provider.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.provider.model.jsonrpc.RpcResult;
import io.nuls.provider.model.jsonrpc.RpcResultError;
import io.nuls.provider.rpctools.LegderTools;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiType;

import java.util.List;

@Controller
@Api(type = ApiType.JSONRPC)
public class LegerController {


    @Autowired
    private LegderTools legderTools;

    @RpcMethod("getAllAsset")
    public RpcResult getAllAsset(List<Object> params) {
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }

        Result<List> result = legderTools.getAllAsset(chainId);
        RpcResult rpcResult = new RpcResult();
        if (result.isFailed()) {
            return rpcResult.setError(new RpcResultError(result.getStatus(), result.getMessage(), null));
        }
        return rpcResult.setResult(result.getList());
    }
}
