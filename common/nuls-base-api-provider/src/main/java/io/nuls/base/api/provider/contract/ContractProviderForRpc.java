package io.nuls.base.api.provider.contract;

import io.nuls.base.api.provider.BaseRpcService;
import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.contract.facade.*;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.core.parse.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-23 14:36
 * @Description: 功能描述
 */
@Provider(Provider.ProviderType.RPC)
public class ContractProviderForRpc extends BaseRpcService implements ContractProvider {

    @Override
    protected <T, R> Result<T> call(String method, Object req, Function<R, Result> callback) {
        return callRpc(ModuleE.SC.abbr, method, req, callback);
    }

    @Override
    public Result<Map> createContract(CreateContractReq req) {
        return callResutlMap("sc_create", req);
    }

    @Override
    public Result<Map> getContractConstructorArgs(GetContractConstructorArgsReq req) {
        return callResutlMap("sc_constructor", req);
    }

    @Override
    public Result<String> callContract(CallContractReq req) {
        return callReturnString("sc_call", req, "txHash");
    }

    @Override
    public Result<Map> viewContract(ViewContractReq req) {
        return callResutlMap("sc_invoke_view", req);
    }

    @Override
    public Result<String> deleteContract(DeleteContractReq req) {
        return callReturnString("sc_delete", req, "txHash");
    }

    @Override
    public Result<Map> getContractTx(GetContractTxReq req) {
        return callResutlMap("sc_contract_tx", req);
    }

    @Override
    public Result<Map> getContractResult(GetContractResultReq req) {
        return callResutlMap("sc_contract_result", req);
    }

    @Override
    public Result<Map> getContractInfo(GetContractInfoReq req) {
        return callResutlMap("sc_contract_info", req);
    }

    @Override
    public Result<String> transferToContract(TransferToContractReq req) {
        return callReturnString("sc_transfer", req, "txHash");
    }

    @Override
    public Result<String> tokenTransfer(TokenTransferReq req) {
        return callReturnString("sc_token_transfer", req, "txHash");
    }

    @Override
    public Result<AccountContractInfo> getAccountContractList(GetAccountContractListReq req) {
        Function<Map<String, Object>, Result> callback = res -> {
            List<Map<String, Object>> list = (List<Map<String, Object>>) res.get("list");
            List<AccountContractInfo> resData = list.stream().map(d -> {
                d.put("createTime", NulsDateUtils.timeStamp2DateStr((Long) d.get("createTime")));
                return MapUtils.mapToBean(d, new AccountContractInfo());
            }).collect(Collectors.toList());
            return success(resData);
        };
        return call("sc_account_contracts", req, callback);
    }

    private <T> Result<T> _call(String method, Object req, Function<Map, Result> callback) {
        return call(method, req, callback);
    }

}
