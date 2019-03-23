package io.nuls.api.provider.contract;

import io.nuls.api.provider.BaseRpcService;
import io.nuls.api.provider.Provider;
import io.nuls.api.provider.Result;
import io.nuls.api.provider.contract.facade.*;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.model.DateUtils;
import io.nuls.tools.parse.MapUtils;

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

    @Override
    public Result<String> callContract(CallContractReq req) {
        return callReturnString("sc_call",req,"txHash");
    }

    @Override
    public Result<Map> viewContract(ViewContractReq req) {
        return callResutlMap("sc_invoke_view",req);
    }

    @Override
    public Result<String> deleteContract(DeleteContractReq req) {
        return callReturnString("sc_delete",req,"txHash");
    }

    @Override
    public Result<Map> getContractTx(GetContractTxReq req) {
        return callResutlMap("sc_contract_tx",req);
    }

    @Override
    public Result<Map> getContractResult(GetContractResultReq req) {
        return callResutlMap("sc_contract_result",req);
    }

    @Override
    public Result<Map> getContractInfo(GetContractInfoReq req) {
        return callResutlMap("sc_contract_info",req);
    }

    @Override
    public Result<String> transferToContract(TransferToContractReq req) {
        return callReturnString("sc_transfer",req,"txHash");
    }

    @Override
    public Result<String> tokenTransfer(TokenTransferReq req) {
        return callReturnString("sc_token_transfer",req,"txHash");
    }

    @Override
    public Result<AccountContractInfo> getAccountContractList(GetAccountContractListReq req) {
        Function<Map<String,Object>,Result> callback = res->{
            AccountContractInfo info = new AccountContractInfo();
            MapUtils.mapToBean(res,info);
            info.setCreateTime(DateUtils.timeStamp2DateStr((Long) res.get("createTime")));
            return success(info);
        };
        return call("",req,callback);
    }

    private <T> Result<T> _call(String method, Object req, Function<Map, Result> callback){
        return call(method,req,callback);
    }

}
