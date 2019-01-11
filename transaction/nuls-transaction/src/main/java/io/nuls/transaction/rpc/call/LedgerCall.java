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
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.model.bo.VerifyCoinDataResult;

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

    /**
     * 验证CoinData
     * @param chain
     * @param txHex
     * @return
     */
    public static VerifyCoinDataResult verifyCoinData(Chain chain, String txHex, boolean batch) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chain.getChainId());
            params.put("txHex", txHex);
            params.put("isBatchValidate", batch);
            HashMap result = (HashMap) TransactionCall.request("validateCoinData", ModuleE.LG.abbr, params);
            return new VerifyCoinDataResult((int)result.get("validateCode"), (String)result.get("validateDesc"));
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 验证CoinData
     * @param chain
     * @param tx
     * @return
     */
    public static VerifyCoinDataResult verifyCoinData(Chain chain, Transaction tx, boolean batch) throws NulsException {
        try {
            return verifyCoinData(chain, tx.hex(), batch);
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 查资产是否存在
     * @param chainId
     * @param assetId
     * @return
     */
  /*  public static boolean verifyAssetExist(int chainId, int assetId) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY_8);
            params.put("chianId", chainId);
            params.put("assetId", assetId);
            HashMap result = (HashMap) TransactionCall.request("cm_asset", ModuleE.LG.abbr, params);
            return null != result;
        } catch (NulsException e) {
            throw new NulsException(e);
        }
    }*/

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
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chain.getChainId());
            params.put("address", address);
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            HashMap result = (HashMap) TransactionCall.request("getNonce", ModuleE.LG.abbr, params);
            String nonce = (String) result.get("nonce");
            return HexUtil.decode(nonce);
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 查询账户特定资产的余额
     * Check the balance of an account-specific asset
     */
    public static BigInteger getBalance(Chain chain, byte[] address, int assetChainId, int assetId) throws NulsException {
        try {
            String addressString = AddressTool.getStringAddressByBytes(address);
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chain.getChainId());
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            params.put("address", addressString);
            HashMap result = (HashMap)TransactionCall.request(ModuleE.AC.abbr, "lg_getBalance", params);
            return BigIntegerUtils.stringToBigInteger((String) result.get("available"));
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    public static void coinDataBatchNotify(Chain chain) throws NulsException {
        //todo 发送给账本，coinData统一验证的通知

    }

    /**
     * 发送交易给账本
     * @param chain
     * @param tx
     * @param comfirmed 是否是已确认的交易
     */
    public static boolean commitTxLedger(Chain chain, Transaction tx, boolean comfirmed) throws NulsException {

        //todo
        return true;
    }

    /**
     * 根据交易回滚数据
     * @param chain
     * @param tx
     * @param comfirmed 是否是已确认的交易
     */
    public static boolean rollbackTxLedger(Chain chain, Transaction tx, boolean comfirmed) throws NulsException {
        //todo
        return true;
    }

}
