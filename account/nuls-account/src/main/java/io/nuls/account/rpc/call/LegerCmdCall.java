package io.nuls.account.rpc.call;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.VerifyTxResult;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.rpc.util.RPCUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.model.BigIntegerUtils;
import io.nuls.tools.model.StringUtils;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * 账本模块接口调用
 *
 * @author: qinyifeng
 * @date: 2018/12/12
 */
public class LegerCmdCall {

    /**
     * 验证CoinData
     * @param chainId
     * @param tx
     * @return
     */
    public static VerifyTxResult verifyCoinData(int chainId, String tx, boolean batch) {
        try {
            Map<String, Object> params = new HashMap<>(AccountConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, AccountConstant.RPC_VERSION);
            params.put("chainId", chainId);
            params.put("tx", tx);
            params.put("isBatchValidate", batch);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "validateCoinData", params);
            if (!cmdResp.isSuccess()) {
                Log.error("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", ModuleE.LG.abbr, "validateCoinData", cmdResp.getResponseComment());
                throw new NulsException(AccountErrorCode.FAILED);
            }
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("validateCoinData");
            return new VerifyTxResult((int)result.get("validateCode"), (String)result.get("validateDesc"));

        } catch (Exception e) {
            Log.error("Calling remote interface failed. module:{} - interface:{}", ModuleE.LG.abbr, "validateCoinData");
            e.printStackTrace();
            return new VerifyTxResult(VerifyTxResult.OTHER_EXCEPTION, "Call validateCoinData failed!");
        }
    }

    /**
     * 验证CoinData
     * @param chainId
     * @param tx
     * @return
     */
    public static VerifyTxResult verifyCoinData(int chainId, Transaction tx) throws NulsException {
        try {
            return verifyCoinData(chainId, RPCUtil.encode(tx.serialize()), false);
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }


    /**
     * 查询账户余额
     */
    public static HashMap getBalanceNonce(int chainId, int assetChainId, int assetId, String address) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            params.put("address", address);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBalanceNonce", params);
            if (!cmdResp.isSuccess()) {
                Log.error("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", ModuleE.LG.abbr, "getBalanceNonce", cmdResp.getResponseComment());
                throw new NulsException(AccountErrorCode.FAILED);
            }
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("getBalanceNonce");
            return result;
        } catch (Exception e) {
            Log.error("Calling remote interface failed. module:{} - interface:{}", ModuleE.LG.abbr, "getBalanceNonce");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询账户余额
     */
    public static BigInteger getBalance(int chainId, int assetChainId, int assetId, String address) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            params.put("address", address);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBalance", params);
            if (!cmdResp.isSuccess()) {
                Log.error("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", ModuleE.LG.abbr, "getBalance", cmdResp.getResponseComment());
                throw new NulsException(AccountErrorCode.FAILED);
            }
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("getBalance");
            Object available = result.get("available");
            return BigIntegerUtils.stringToBigInteger(String.valueOf(available));
        } catch (Exception e) {
            Log.error("Calling remote interface failed. module:{} - interface:{}", ModuleE.LG.abbr, "getBalance");
            e.printStackTrace();
        }
        return new BigInteger("0");
    }

    /**
     * 查询账户交易随机数
     */
    public static byte[] getNonce(int chainId, int assetChainId, int assetId, String address) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            params.put("address", address);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getNonce", params);
            if (!cmdResp.isSuccess()) {
                Log.error("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", ModuleE.LG.abbr, "getNonce", cmdResp.getResponseComment());
                throw new NulsException(AccountErrorCode.FAILED);
            }
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("getNonce");
            String nonce = (String) result.get("nonce");
            if (StringUtils.isNotBlank(nonce)) {
                return RPCUtil.decode(nonce);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
