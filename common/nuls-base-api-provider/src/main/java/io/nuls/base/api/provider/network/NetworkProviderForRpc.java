package io.nuls.base.api.provider.network;

import io.nuls.base.api.provider.BaseReq;
import io.nuls.base.api.provider.BaseRpcService;
import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.network.facade.NetworkInfo;
import io.nuls.base.api.provider.network.facade.RemoteNodeInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.parse.MapUtils;

import java.util.ArrayList;
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
            List<String> list = new ArrayList<>();
            res.stream().forEach(map->{
                RemoteNodeInfo info = dataToRemoteNodeInfo((Map<String, Object>) map);
                list.add(info.getPeer());
            });
            return success(list);
        };
        return call("nw_nodes",req,callback);
    }

    @Override
    public Result<RemoteNodeInfo> getNodesInfo() {
        BaseReq req = new BaseReq();
        req.setChainId(getChainId());
        Function<List,Result> callback = res->{
            List<RemoteNodeInfo> list = new ArrayList<>();
            res.stream().forEach(map->{
                RemoteNodeInfo info = dataToRemoteNodeInfo((Map<String, Object>) map);
                list.add(info);
            });
            return success(list);
        };
        return call("nw_nodes",req,callback);
    }

    private RemoteNodeInfo dataToRemoteNodeInfo(Map<String,Object> data){
        RemoteNodeInfo info = MapUtils.mapToBean(data,new RemoteNodeInfo());
        return info;
    }

}
