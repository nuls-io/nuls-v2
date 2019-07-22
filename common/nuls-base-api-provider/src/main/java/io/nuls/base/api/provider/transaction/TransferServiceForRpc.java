package io.nuls.base.api.provider.transaction;

import io.nuls.base.RPCUtil;
import io.nuls.base.api.provider.BaseReq;
import io.nuls.base.api.provider.BaseRpcService;
import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.transaction.facade.*;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Coin;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.model.DateUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.MapUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.MessageUtil;
import io.nuls.core.rpc.model.message.Request;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.util.HashMap;
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
public class TransferServiceForRpc extends BaseRpcService implements TransferService {

    @Override
    public Result transferTest(int method, String addr1, String addr2) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("act", method);
            params.put("address1", addr1);
            params.put("address2", addr2);
//            callRpc(ModuleE.AC.abbr,method,params);
//            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "transferCMDTest", params);
            Request request = MessageUtil.newRequest("transferCMDTest", params, Constants.BOOLEAN_TRUE, Constants.ZERO, Constants.ZERO);
            String messageId = ResponseMessageProcessor.requestOnly(ModuleE.TX.abbr, request);
//            return response.isSuccess() ? Result.fail("","") : new Result();
            return messageId.equals("0") ? Result.fail("","") : new Result();
        } catch (Exception e) {
            return Result.fail("","fail");
        }

    }

    @Override
    public Result<String> transfer(TransferReq req) {
        return callReturnString("ac_transfer",req,"value");
    }

    @Override
    public Result<CreateMultiSignTransferRes> multiSignTransfer(CreateMultiSignTransferReq req) {
        return callRpc(ModuleE.AC.abbr,"ac_createMultiSignTransfer",req,(Function<Map,Result>)(data->{
            CreateMultiSignTransferRes res = MapUtils.mapToBean(data,new CreateMultiSignTransferRes());
            return success(res);
        }));
    }

    @Override
    public Result<CreateMultiSignTransferRes> signMultiSignTransfer(SignMultiSignTransferReq req) {
        return callRpc(ModuleE.AC.abbr,"ac_signMultiSignTransaction",req,(Function<Map,Result>)(data->{
            CreateMultiSignTransferRes res = MapUtils.mapToBean(data,new CreateMultiSignTransferRes());
            return success(res);
        }));
    }

    @Override
    public Result<String> transferByAlias(TransferReq req) {
        return callReturnString("ac_transfer",req,"value");
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
        return callRpc(ModuleE.TX.abbr,"tx_getTxClient",req,
                (Function<Map,Result>)res->tranderTransactionData(tranderTransaction(res))
        );
    }

    @Override
    protected  <T,R> Result<T> call(String method, Object req, Function<R,Result> res){
        return callRpc(ModuleE.AC.abbr,method,req,res);
    }

    private Result<Transaction> getTx(String method, BaseReq req){
        return callRpc(ModuleE.TX.abbr,method,req,(Function<Map,Result>)this::tranderTransaction);
    }

    private Result<Transaction> tranderTransaction(Map<String,Object> data){
        try {
            String hexString = (String) data.get("tx");
            if(StringUtils.isNull(hexString)){
                return fail(CommonCodeConstanst.DATA_NOT_FOUND,"not found tx");
            }
            Transaction transaction = new Transaction();
            transaction.parse(new NulsByteBuffer(RPCUtil.decode(hexString)));
            transaction.setBlockHeight(Long.parseLong(String.valueOf(data.get("height"))));
            Integer state = (Integer) data.get("status");
            transaction.setStatus(state == TxStatusEnum.UNCONFIRM.getStatus() ? TxStatusEnum.UNCONFIRM : TxStatusEnum.CONFIRMED);
            return success(transaction);
        } catch (NulsException e) {
            Log.error("反序列化transaction发生异常",e);
            return fail(CommonCodeConstanst.DESERIALIZE_ERROR);
        }
    }

    private Result<TransactionData> tranderTransactionData(Result<Transaction>  data){
        if(data.isFailed())return fail(ErrorCode.init(data.getStatus()),data.getMessage());
        try {
            Transaction transaction = data.getData();
            TransactionData res = new TransactionData();
            res.setBlockHeight(transaction.getBlockHeight());
            res.setStatus(transaction.getStatus());
            res.setHash(transaction.getHash().toString());
            res.setRemark(ByteUtils.asString(transaction.getRemark()));
            res.setInBlockIndex(transaction.getInBlockIndex());
            res.setSize(transaction.getSize());
            res.setTime(DateUtils.timeStamp2DateStr(transaction.getTime()*1000));
            res.setTransactionSignature(RPCUtil.encode(transaction.getTransactionSignature()));
            res.setType(transaction.getType());
            res.setForm(transaction.getCoinDataInstance().getFrom().stream().map(coinData->{
                TransactionCoinData tcd = buildTransactionCoinData(coinData);
                tcd.setNonce(HexUtil.encode(coinData.getNonce()));
                return tcd;
            }).collect(Collectors.toList()));
            res.setTo(transaction.getCoinDataInstance().getTo().stream().map(this::buildTransactionCoinData).collect(Collectors.toList()));
            return success(res);
        } catch (NulsException e) {
            Log.error("反序列化transaction发生异常",e);
            return fail(CommonCodeConstanst.DESERIALIZE_ERROR);
        }
    }

    private TransactionCoinData buildTransactionCoinData(Coin coinData){
        TransactionCoinData tcd = new TransactionCoinData();
        tcd.setAddress(AddressTool.getStringAddressByBytes(coinData.getAddress()));
        tcd.setAmount(coinData.getAmount());
        tcd.setAssetsChainId(coinData.getAssetsChainId());
        tcd.setAssetsId(coinData.getAssetsId());
        return tcd;
    }

}
