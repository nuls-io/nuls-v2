package io.nuls.api.provider.consensus;

import io.nuls.api.provider.BaseRpcService;
import io.nuls.api.provider.Provider;
import io.nuls.api.provider.Result;
import io.nuls.api.provider.consensus.facade.CreateAgentReq;
import io.nuls.api.provider.consensus.facade.DepositToAgentReq;
import io.nuls.api.provider.consensus.facade.StopAgentReq;
import io.nuls.api.provider.consensus.facade.WithdrawReq;
import io.nuls.rpc.model.ModuleE;

import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 11:59
 * @Description: 共识
 */
@Provider(Provider.ProviderType.RPC)
public class ConsensusProviderForRpc extends BaseRpcService implements ConsensusProvider {

    @Override
    protected <T, R> Result<T> call(String method, Object req, Function<R, Result> callback) {
        return callRpc(ModuleE.CS.abbr,method,req,callback);
    }

    @Override
    public Result<String> createAgent(CreateAgentReq req) {
        return callReturnString("cs_createAgent",req,"txHash");
    }

    @Override
    public Result<String> stopAgent(StopAgentReq req) {
        return callReturnString("cs_stopAgent",req,"txHash");
    }

    @Override
    public Result<String> depositToAgent(DepositToAgentReq req) {
        return callReturnString("cs_depositToAgent",req,"txHash");
    }

    @Override
    public Result<String> withdraw(WithdrawReq req) {
        return callReturnString("cs_withdraw",req,"txHash");
    }
}
