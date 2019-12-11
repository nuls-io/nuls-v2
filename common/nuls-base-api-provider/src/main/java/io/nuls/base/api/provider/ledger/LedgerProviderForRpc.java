package io.nuls.base.api.provider.ledger;

import io.nuls.base.api.provider.BaseRpcService;
import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ledger.facade.AccountBalanceInfo;
import io.nuls.base.api.provider.ledger.facade.GetBalanceReq;
import io.nuls.base.api.provider.ledger.facade.RegLocalAssetReq;
import io.nuls.core.rpc.model.ModuleE;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 13:44
 * @Description: 功能描述
 */
@Provider(Provider.ProviderType.RPC)
public class LedgerProviderForRpc extends BaseRpcService implements LedgerProvider {

    @Override
    protected <T, R> Result<T> call(String method, Object req, Function<R, Result> callback) {
        return callRpc(ModuleE.LG.abbr,method,req,callback);
    }

    @Override
    public Result<AccountBalanceInfo> getBalance(GetBalanceReq req) {
        Function<Map,Result> callback = res->{
            BigInteger total = new BigInteger(String.valueOf(res.get("total")));
            BigInteger freeze = new BigInteger(String.valueOf(res.get("freeze")));
            BigInteger available = new BigInteger(String.valueOf(res.get("available")));
            AccountBalanceInfo info = new AccountBalanceInfo();
            info.setAvailable(available);
            info.setFreeze(freeze);
            info.setTotal(total);
            return success(info);
        };
        return call("getBalance",req,callback);
    }

    @Override
    public Result<Map> regLocalAsset(RegLocalAssetReq req) {
        return callResutlMap("chainAssetTxReg", req);
    }
}
