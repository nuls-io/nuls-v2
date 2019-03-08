package io.nuls.api.provider.transaction;

import io.nuls.api.provider.BaseRpcService;
import io.nuls.api.provider.Provider;
import io.nuls.api.provider.Result;
import io.nuls.api.provider.transaction.facade.TransferReq;
import io.nuls.rpc.model.ModuleE;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 17:00
 * @Description: 功能描述
 */
@Provider(Provider.ProviderType.RPC)
@Slf4j
public class TransferServiceForRpc extends BaseRpcService implements TransferService {

    @Override
    public Result<String> transfer(TransferReq req) {
        return callReturnString("ac_transfer",req,"value");
    }

    private <T> Result<T> call(String method, Object req, Function<Map,Result> res){
        return callRpc(ModuleE.AC.abbr,method,req,res);
    }

    private Result<String> callReturnString(String method,Object req, String fieldName){
        return call(method,req,res->{
            String data = (String) res.get(fieldName);
            return success(data);
        });
    }


}
