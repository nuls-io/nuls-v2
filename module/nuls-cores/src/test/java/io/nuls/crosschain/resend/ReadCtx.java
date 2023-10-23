package io.nuls.crosschain.resend;

import io.nuls.base.data.Transaction;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.model.po.CtxStatusPO;
import io.nuls.crosschain.rpc.call.NetWorkCall;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2020/8/7 10:29
 * @Description: 功能描述
 */
public class ReadCtx {

    static int CHAIN_ID = 9;

    public static void main(String[] args) throws Exception {
        NoUse.mockModule();
        Log.info("reday");
        RocksDBService.init("/Users/zhoulijun/workspace/nuls/nuls_2.0/NULS_Wallet/cross-chain");
        List<String> hashList = new ArrayList<>();
        hashList.add("aa7433c8cb9dd86c97be05e0cfe47c9a30423a1e1e1f494fd4b71a389ab73fd7");
        hashList.forEach(hash->{
            resetBroadcast(hash);
        });
    }

    public static void reSend(String hash){
        byte[] b = RocksDBService.get("new_ctx_status1", HexUtil.decode(hash));
        Log.info("{}", HexUtil.encode(b));
        CtxStatusPO ctx = new CtxStatusPO();
        try {
            ctx.parse(b,0);
            Transaction tx = ctx.getTx();
            TransactionSignature signature = new TransactionSignature();
            signature.parse(tx.getTransactionSignature(),0);
            Log.info("{}",signature.getSignersCount());
            Log.info("txHex:{}", HexUtil.encode(tx.serialize()));
            Log.info("{}",sendTx(HexUtil.encode(tx.serialize())));
        } catch (NulsException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void resetBroadcast(String hash){
        byte[] b = RocksDBService.get("new_ctx_status1", HexUtil.decode(hash));
        Log.info("{}", HexUtil.encode(b));
        CtxStatusPO ctx = new CtxStatusPO();
        try {
            ctx.parse(b,0);
            Transaction tx = ctx.getTx();
            TransactionSignature signature = new TransactionSignature();
            signature.parse(tx.getTransactionSignature(),0);
            signature.getP2PHKSignatures().forEach(d->{
                Log.info("p:{}",HexUtil.encode(d.getPublicKey()));
            });
            BroadCtxSignMessage message = new BroadCtxSignMessage();
            message.setLocalHash(tx.getHash());
            message.setSignature(signature.getP2PHKSignatures().get(0).serialize());
            NetWorkCall.broadcast(1, message, CommandConstant.BROAD_CTX_SIGN_MESSAGE, false);
        } catch (NulsException | IOException e) {
            e.printStackTrace();
        }
    }

    public static String sendTx(String tx) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, CHAIN_ID);
        params.put("tx", tx);
        try {
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
            if (!cmdResp.isSuccess()) {
                String errorCode = cmdResp.getResponseErrorCode();
                Log.error("Call interface [{}] error, ErrorCode is {}, ResponseComment:{}",
                        "tx_newTx", errorCode, cmdResp.getResponseComment());
                throw new NulsException(ErrorCode.init(errorCode));
            }
            return JSONUtils.obj2json(cmdResp.getResponseData());
        }catch (Exception e){
            Log.error("fail",e);
            return null;
        }
    }

}
