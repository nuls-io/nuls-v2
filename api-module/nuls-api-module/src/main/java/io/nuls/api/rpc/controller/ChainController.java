package io.nuls.api.rpc.controller;

import io.nuls.api.ApiContext;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.tools.core.annotation.Controller;
import io.nuls.tools.core.annotation.RpcMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChainController {

    @RpcMethod("getChains")
    public RpcResult getChains(List<Object> params) {
        Map<String, Object> map = new HashMap<>();
        map.put("default", ApiContext.defaultChainId);
        map.put("list", CacheManager.getApiCaches().keySet());

        return RpcResult.success(map);
    }

    @RpcMethod("getCoinInfo")
    public RpcResult getCoinInfo(List<Object> params) {

        ApiContext.NULS_MAP.put("total", 0L);
        ApiContext.NULS_MAP.put("consensusTotal", 0L);
        ApiContext.NULS_MAP.put("circulation", 0L);
        return RpcResult.success(ApiContext.NULS_MAP);
    }
}
