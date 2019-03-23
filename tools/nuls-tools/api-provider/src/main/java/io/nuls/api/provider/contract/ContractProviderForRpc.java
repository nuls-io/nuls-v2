package io.nuls.api.provider.contract;

import io.nuls.api.provider.BaseRpcService;
import io.nuls.api.provider.Provider;
import io.nuls.api.provider.Result;
import io.nuls.api.provider.contract.facade.CreateContractReq;
import io.nuls.api.provider.contract.facade.GetContractConstructorArgsReq;
import io.nuls.rpc.model.ModuleE;

import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 14:36
 * @Description: 功能描述
 */
@Provider(Provider.ProviderType.RPC)
public class ContractProviderForRpc extends BaseRpcService implements ContractProvider {

    @Override
    protected <T, R> Result<T> call(String method, Object req, Function<R, Result> callback) {
        return callRpc(ModuleE.SC.abbr,method,req,callback);
    }

    @Override
    public Result<Map> createContract(CreateContractReq req) {
        return callResutlMap("sc_create",req);
    }

    @Override
    public Result<Map> getContractConstructorArgs(GetContractConstructorArgsReq req) {
        return callResutlMap("sc_constructor",req);
    }

    private <T> Result<T> _call(String method, Object req, Function<Map, Result> callback){
        return call(method,req,callback);
    }

    private Result<Map> callResutlMap(String method,Object req){
        return _call(method,req,res->{
            Map<String, String> list = (Map<String, String>) res;
            return success(list);
        });
    }

}
