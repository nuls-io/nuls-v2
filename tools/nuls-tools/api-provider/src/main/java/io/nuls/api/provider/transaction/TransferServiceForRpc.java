package io.nuls.api.provider.transaction;

import io.nuls.api.provider.BaseReq;
import io.nuls.api.provider.BaseRpcService;
import io.nuls.api.provider.Provider;
import io.nuls.api.provider.Result;
import io.nuls.api.provider.transaction.facade.*;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.constant.TxStatusEnum;
import io.nuls.base.data.Coin;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.model.DateUtils;
import io.nuls.tools.model.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public Result<TransactionData> getSimpleTxDataByHash(GetConfirmedTxByHashReq req) {
        Function<Map,Result> callback = res->tranderTransactionData(tranderTransaction(res));
        return callRpc(ModuleE.TX.abbr,"tx_getConfirmedTxClient",req,callback);
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

    private Result<Transaction> tranderTransaction(Map<String,Object> data){
        try {
            String hexString = (String) data.get("txHex");
            if(hexString == null){
                return fail(ERROR_CODE,"not found tx");
            }
            Transaction transaction = new Transaction();
            transaction.parse(new NulsByteBuffer(HexUtil.decode(hexString)));
            transaction.setBlockHeight(Long.parseLong(String.valueOf(data.get("height"))));
            Integer state = (Integer) data.get("status");
            transaction.setStatus(state == TxStatusEnum.UNCONFIRM.getStatus() ? TxStatusEnum.UNCONFIRM : TxStatusEnum.CONFIRMED);
            return success(transaction);
        } catch (NulsException e) {
            log.error("反序列化transaction发生异常",e);
            return fail(ERROR_CODE);
        }
    }

    private Result<TransactionData> tranderTransactionData(Result<Transaction>  data){
        try {
            Transaction transaction = data.getData();
            TransactionData res = new TransactionData();
            res.setBlockHeight(transaction.getBlockHeight());
            res.setStatus(transaction.getStatus());
            res.setHash(transaction.getHash().toString());
            res.setRemark(HexUtil.encode(transaction.getRemark()));
            res.setInBlockIndex(transaction.getInBlockIndex());
            res.setSize(transaction.getSize());
            res.setTime(DateUtils.timeStamp2DateStr(transaction.getTime()));
            res.setTransactionSignature(HexUtil.encode(transaction.getTransactionSignature()));
            res.setType(transaction.getType());
            res.setForm(transaction.getCoinDataInstance().getFrom().stream().map(coinData->{
                TransactionCoinData tcd = buildTransactionCoinData(coinData);
                tcd.setNonce(HexUtil.encode(coinData.getNonce()));
                return tcd;
            }).collect(Collectors.toList()));
            res.setTo(transaction.getCoinDataInstance().getTo().stream().map(this::buildTransactionCoinData).collect(Collectors.toList()));
            return success(res);
        } catch (NulsException e) {
            log.error("反序列化transaction发生异常",e);
            return fail(ERROR_CODE);
        }
    }

    private TransactionCoinData buildTransactionCoinData(Coin coinData){
        TransactionCoinData tcd = new TransactionCoinData();
        tcd.setAddress(HexUtil.encode(coinData.getAddress()));
        tcd.setAmount(coinData.getAmount());
        tcd.setAssetsChainId(coinData.getAssetsChainId());
        tcd.setAssetsId(coinData.getAssetsId());
        return tcd;
    }

}
