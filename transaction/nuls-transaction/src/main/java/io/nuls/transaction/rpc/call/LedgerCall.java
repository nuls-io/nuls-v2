package io.nuls.transaction.rpc.call;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Address;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
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
 * @date: 2018/12/20
 */
public class LedgerCall {


    public static void coinDataBatchNotify(Chain chain) {
        //todo 发送给账本，coinData统一验证的通知

    }

    /**
     * 验证CoinData
     * @param chain
     * @param txHex
     * @return
     */
    public static boolean verifyCoinData(Chain chain, String txHex, boolean batch) {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chain.getChainId());
        params.put("txHex", txHex);
        try {
            //单个or批量
            String cmd = batch ? "lg_validateCoinData": "lg_validateCoinData";
            HashMap result = (HashMap) TransactionCall.request(cmd, ModuleE.LG.abbr, params);
            return (Boolean) result.get("value");
        } catch (Exception e) {
            chain.getLogger().info(e.getMessage(), e.fillInStackTrace());
            return false;
        }
    }

    /**
     * 验证CoinData
     * @param chain
     * @param tx
     * @return
     */
    public static boolean verifyCoinData(Chain chain, Transaction tx, boolean batch) {
        //todo 验证CoinData
        try {
            return verifyCoinData(chain, tx.hex(), batch);
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
    }

    /**
     * 查询nonce值
     *
     * @param chain
     * @param address
     * @param assetChainId
     * @param assetId
     * @return
     * @throws NulsException
     */
    public static byte[] getNonce(Chain chain, String address, int assetChainId, int assetId) throws NulsException {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chain.getChainId());
        params.put("address", address);
        params.put("assetChainId", assetChainId);
        params.put("assetId", assetId);
        try {
            HashMap result = (HashMap) TransactionCall.request("getNonce", ModuleE.LG.abbr, params);
            String nonce = (String) result.get("nonce");
            return HexUtil.decode(nonce);
        } catch (Exception e) {
            chain.getLogger().info(e.getMessage(), e.fillInStackTrace());
            return null;
        }
    }

    /**
     * 查询账户特定资产的余额
     * Check the balance of an account-specific asset
     */
    public static BigInteger getBalance(Chain chain, byte[] address, int assetChainId, int assetId) {
        String addressString = AddressTool.getStringAddressByBytes(address);
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", chain.getChainId());
        params.put("assetChainId", assetChainId);
        params.put("assetId", assetId);
        params.put("address", addressString);
        try {
            HashMap result = (HashMap)TransactionCall.request(ModuleE.AC.abbr, "lg_getBalance", params);
            return BigIntegerUtils.stringToBigInteger((String) result.get("available"));
        } catch (Exception e) {
            Log.error(e);
        }
        return new BigInteger("0");
    }

    /**
     * 发送交易给账本
     * @param chain
     * @param tx
     * @param comfirmed 是否是已确认的交易
     */
    public static boolean commitTxLedger(Chain chain, Transaction tx, boolean comfirmed){
        //todo
        return true;
    }

    /**
     * 根据交易回滚数据
     * @param chain
     * @param tx
     * @param comfirmed 是否是已确认的交易
     */
    public static boolean rollbackTxLedger(Chain chain, Transaction tx, boolean comfirmed){
        //todo
        return true;
    }

}
