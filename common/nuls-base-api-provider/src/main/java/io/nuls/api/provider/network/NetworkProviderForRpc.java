package io.nuls.api.provider.network;

import io.nuls.api.provider.BaseReq;
import io.nuls.api.provider.BaseRpcService;
import io.nuls.api.provider.Provider;
import io.nuls.api.provider.Result;
import io.nuls.api.provider.network.facade.NetworkInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.parse.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 16:16
 * @Description: 功能描述
 */
@Provider(Provider.ProviderType.RPC)
public class NetworkProviderForRpc extends BaseRpcService implements NetworkProvider {
    @Override
    protected <T, R> Result<T> call(String method, Object req, Function<R, Result> callback) {
        return callRpc(ModuleE.NW.abbr,method,req,callback);
    }

    @Override
    public Result<NetworkInfo> getInfo() {
        BaseReq req = new BaseReq();
        req.setChainId(getChainId());
        Function<Map,Result> callback = res->{
            NetworkInfo info = MapUtils.mapToBean(res,new NetworkInfo());
            return success(info);
        };
        return call("nw_info",req,callback);
    }

    @Override
    public Result<String> getNodes() {
        BaseReq req = new BaseReq();
        req.setChainId(getChainId());
        Function<List,Result> callback = res->{
            return success(res);
        };
        return call("nw_nodes",req,callback);
    }
}
