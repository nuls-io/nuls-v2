package io.nuls.api.provider.consensus;

import io.nuls.api.provider.BaseRpcService;
import io.nuls.api.provider.Provider;
import io.nuls.api.provider.Result;
import io.nuls.api.provider.consensus.facade.*;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.constant.CommonCodeConstanst;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.MapUtils;

import java.util.List;
import java.util.Map;
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

    @Override
    public Result<AgentInfo> getAgentInfo(GetAgentInfoReq req) {
        return call("cs_getAgentInfo",req,(Function<Map, Result>)res->{
            if(res == null){
                return fail(RPC_ERROR_CODE,"agent not found");
            }
            AgentInfo agentInfo = MapUtils.mapToBean(res,new AgentInfo());

            return success(agentInfo);
        });
    }

    @Override
    public Result<AgentInfo> getAgentList(GetAgentListReq req) {
        return call("cs_getAgentList",req, (Function<Map, Result>) res -> {
            try {
                List<AgentInfo> list = MapUtils.mapsToObjects((List<Map<String, Object>>) res.get("list"),AgentInfo.class);
                return success(list);
            } catch (InstantiationException e) {
                Log.error("cs_getAgentList fail",e);
                return fail(CommonCodeConstanst.FAILED);
            } catch (IllegalAccessException e) {
                Log.error("cs_getAgentList fail",e);
                return fail(CommonCodeConstanst.FAILED);
            }
        });
    }


}
