package io.nuls.base.api.provider.protocol;

import io.nuls.base.api.provider.BaseRpcService;
import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.protocol.facade.GetVersionReq;
import io.nuls.base.api.provider.protocol.facade.VersionInfo;
import io.nuls.core.rpc.model.ModuleE;

import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2020-01-15 18:17
 * @Description: 功能描述
 */
@Provider(Provider.ProviderType.RPC)
public class ProtocolProviderForRpc extends BaseRpcService implements ProtocolProvider {

    @Override
    protected <T, R> Result<T> call(String method, Object req, Function<R, Result> callback) {
        return callRpc(ModuleE.PU.abbr,method,req,callback);
    }

    @Override
    public  Result<VersionInfo> getVersion(GetVersionReq req) {
        Function<Map,Result> callback = res->{
            Map<String,Object> local = (Map<String, Object>) res.get("localProtocolVersion");
            Map<String,Object> net = (Map<String, Object>) res.get("currentProtocolVersion");
            VersionInfo info = new VersionInfo();
            info.setLocalProtocolVersion((Integer) local.get("version"));
            info.setCurrentProtocolVersion((Integer) net.get("version"));
            return success(info);
        };
        return call("getVersion",req,callback);
    }
}
