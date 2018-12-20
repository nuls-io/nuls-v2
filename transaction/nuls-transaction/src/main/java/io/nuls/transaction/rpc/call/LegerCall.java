package io.nuls.transaction.rpc.call;

import io.nuls.base.data.Transaction;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: qinyifeng
 * @date: 2018/12/05
 */
public class LegerCall {

    public static boolean verifyCoinData(Chain chain, String txHex){
        //todo 验证CoinData
        /*try {

            return true;
        } catch (NulsException e){
            chain.getLogger().info(e.getErrorCode().getMsg(), e.fillInStackTrace());
            return false;
        }*/
        return true;
    }
    public static boolean verifyCoinData(Chain chain, Transaction tx){
        //todo 验证CoinData
        try {
            return verifyCoinData(chain, tx.hex());
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
    }

    public static byte[] getNonce(byte[] address, int chainId, int assetId) throws NulsException {
        //todo 查nonce
        byte[] nonce = new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        return nonce;
    }

    public static BigInteger getBalance(byte[] address, int chainId, int assetId) throws NulsException {
        //todo 查余额;
        return new BigInteger("10000");
    }

}
