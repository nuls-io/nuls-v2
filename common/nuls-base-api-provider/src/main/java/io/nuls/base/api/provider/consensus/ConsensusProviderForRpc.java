package io.nuls.base.api.provider.consensus;

import io.nuls.base.api.provider.BaseRpcService;
import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.consensus.facade.*;
import io.nuls.base.api.provider.transaction.facade.MultiSignTransferRes;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.log.Log;
import io.nuls.core.parse.MapUtils;
import io.nuls.core.rpc.model.ModuleE;

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
    public Result<MultiSignTransferRes> createAgentForMultiSignAccount(CreateMultiSignAgentReq req) {
        return callRpc(ModuleE.CS.abbr,"cs_createMultiAgent",req,(Function<Map,Result>)(data-> success(MapUtils.mapToBean(data,new MultiSignTransferRes()))));
    }

    @Override
    public Result<String> stopAgent(StopAgentReq req) {
        return callReturnString("cs_stopAgent",req,"txHash");
    }

    @Override
    public Result<MultiSignTransferRes> stopAgentForMultiSignAccount(StopMultiSignAgentReq req) {
        return callRpc(ModuleE.CS.abbr,"cs_stopMultiAgent",req,(Function<Map,Result>)(data-> success(MapUtils.mapToBean(data,new MultiSignTransferRes()))));
    }

    @Override
    public Result<String> depositToAgent(DepositToAgentReq req) {
        return callReturnString("cs_depositToAgent",req,"txHash");
    }

    @Override
    public Result<MultiSignTransferRes> depositToAgentForMultiSignAccount(MultiSignAccountDepositToAgentReq req) {
        return callRpc(ModuleE.CS.abbr,"cs_multiDeposit",req,(Function<Map,Result>)(data-> success(MapUtils.mapToBean(data,new MultiSignTransferRes()))));
    }

    @Override
    public Result<String> withdraw(WithdrawReq req) {
        return callReturnString("cs_withdraw",req,"txHash");
    }

    @Override
    public Result<MultiSignTransferRes> withdrawForMultiSignAccount(MultiSignAccountWithdrawReq req) {
        return callRpc(ModuleE.CS.abbr,"cs_multiWithdraw",req,(Function<Map,Result>)(data-> success(MapUtils.mapToBean(data,new MultiSignTransferRes()))));
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
            } catch (Exception e) {
                Log.error("cs_getAgentList fail",e);
                return fail(CommonCodeConstanst.FAILED);
            }
        });
    }

    @Override
    public Result<DepositInfo> getDepositList(GetDepositListReq req) {
        return call("cs_getDepositList",req, (Function<Map, Result>) res -> {
            try {
                List<DepositInfo> list = MapUtils.mapsToObjects((List<Map<String, Object>>) res.get("list"),DepositInfo.class);
                return success(list);
            } catch (Exception e) {
                Log.error("cs_getDepositList fail",e);
                return fail(CommonCodeConstanst.FAILED);
            }
        });
    }
}
