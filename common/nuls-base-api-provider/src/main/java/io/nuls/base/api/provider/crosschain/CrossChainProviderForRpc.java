package io.nuls.base.api.provider.crosschain;

import io.nuls.base.RPCUtil;
import io.nuls.base.api.provider.BaseRpcService;
import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.*;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.MapUtils;
import io.nuls.core.rpc.model.ModuleE;

import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-06 17:14
 * @Description: 功能描述
 */
@Provider(Provider.ProviderType.RPC)
public class CrossChainProviderForRpc extends BaseRpcService implements CrossChainProvider {


    @Override
    protected <T, R> Result<T> call(String method, Object req, Function<R, Result> callback) {
        return callRpc(ModuleE.CC.abbr,method,req,callback);
    }

    @Override
    public Result<String> createCrossTx(CreateCrossTxReq req) {
        return callReturnString("createCrossTx",req,"txHash");
    }


    @Override
    public Result<Integer> getCrossTxState(GetCrossTxStateReq req) {
        return _call("getCrossTxState",req,res->{
            if(res == null){
                return fail(RPC_ERROR_CODE,"tx not found");
            }
            Integer data = (Integer) res.get("value");
            return success(data);
        });
    }

    @Override
    public Result<Transaction> getCrossTx(GetCrossTxStateReq req) {
        return call("getCrossChainTxInfoForCtxStatusPO",req,(Function<String,Result>)this::tranderTransaction);
    }

    @Override
    public Result<String> rehandleCtx(RehandleCtxReq req) {
        return  callReturnString("ctxRehandle",req,"msg");
    }

    @Override
    public Result<String> resetLocalVerifier(CreateResetLocalVerifierTxReq req) {
        return callReturnString("createResetLocalVerifierTx",req,"txHash");
    }


    private <T> Result<T> _call(String method, Object req, Function<Map, Result> callback){
        return call(method,req,callback);
    }

    private Result<Transaction> tranderTransaction(String hexString){
        try {
            if(StringUtils.isNull(hexString)){
                return fail(CommonCodeConstanst.DATA_NOT_FOUND,"not found tx");
            }
            Transaction transaction = new Transaction();
            transaction.parse(new NulsByteBuffer(RPCUtil.decode(hexString)));
            return success(transaction);
        } catch (NulsException e) {
            Log.error("反序列化transaction发生异常",e);
            return fail(CommonCodeConstanst.DESERIALIZE_ERROR);
        }
    }


}
