package io.nuls.api.provider.transaction;

import io.nuls.api.provider.BaseReq;
import io.nuls.api.provider.BaseRpcService;
import io.nuls.api.provider.Provider;
import io.nuls.api.provider.Result;
import io.nuls.api.provider.transaction.facade.GetConfirmedTxByHashReq;
import io.nuls.api.provider.transaction.facade.GetTxByHashReq;
import io.nuls.api.provider.transaction.facade.TransferByAliasReq;
import io.nuls.api.provider.transaction.facade.TransferReq;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.constant.TxStatusEnum;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.model.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 17:00
 * @Description:
 * 交易服务
 */
@Provider(Provider.ProviderType.RPC)
@Slf4j
public class TransferServiceForRpc extends BaseRpcService implements TransferService {

    @Override
    public Result<String> transfer(TransferReq req) {
        return callReturnString("ac_transfer",req,"value");
    }

    @Override
    public Result<String> transferByAlias(TransferByAliasReq req) {
        return callReturnString("ac_transferByAlias",req,"txHash");
    }

    @Override
    public Result<Transaction> getTxByHash(GetTxByHashReq req) {
        return getTx("tx_getTxClient",req) ;
    }

    @Override
    public Result<Transaction> getConfirmedTxByHash(GetConfirmedTxByHashReq req) {
        return getTx("tx_getConfirmedTxClient",req);
    }

    @Override
    protected  <T,R> Result<T> call(String method, Object req, Function<R,Result> res){
        return callRpc(ModuleE.AC.abbr,method,req,res);
    }

    private Result<Transaction> getTx(String method, BaseReq req){
        Function<Map,Result> callback = res->{
            return tranderTransaction(res);
        };
        return callRpc(ModuleE.TX.abbr,method,req,callback);
    }

    private Result<Transaction> tranderTransaction(Map<String,Object> res){
        try {
            String hexString = (String) res.get("txHex");
            if(hexString == null){
                return fail(ERROR_CODE,"not found tx");
            }
            Transaction transaction = new Transaction();
            transaction.parse(new NulsByteBuffer(HexUtil.decode(hexString)));
            transaction.setBlockHeight(Long.parseLong(String.valueOf(res.get("height"))));
            Integer state = (Integer) res.get("status");
            transaction.setStatus(state == TxStatusEnum.UNCONFIRM.getStatus() ? TxStatusEnum.UNCONFIRM : TxStatusEnum.CONFIRMED);
            return success(transaction);
        } catch (NulsException e) {
            log.error("反序列化transaction发生异常",e);
            return fail(ERROR_CODE);
        }
    }

}
