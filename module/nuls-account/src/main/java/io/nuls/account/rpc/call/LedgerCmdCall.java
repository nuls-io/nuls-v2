package io.nuls.account.rpc.call;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.VerifyTxResult;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.StringUtils;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * 账本模块接口调用
 *
 * @author: qinyifeng
 * @date: 2018/12/12
 */
public class LedgerCmdCall {

    /**
     * 验证单个交易与未确认交易提交
     * @param chainId
     * @param txStr
     */
    public static VerifyTxResult commitUnconfirmedTx(int chainId, String txStr) {
        try {
            Map<String, Object> params = new HashMap<>(AccountConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, AccountConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("tx", txStr);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "commitUnconfirmedTx", params);
            if (!cmdResp.isSuccess()) {
                Log.error("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", ModuleE.LG.abbr, "commitUnconfirmedTx", cmdResp.getResponseComment());
                throw new NulsException(AccountErrorCode.FAILED);
            }
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("commitUnconfirmedTx");
            return new VerifyTxResult((int)result.get("validateCode"), (String)result.get("validateDesc"));
        } catch (Exception e) {
            return new VerifyTxResult(VerifyTxResult.OTHER_EXCEPTION, "Call validateCoinData failed!");
        }
    }

    /**
     * 调用账本回滚未确认的交易
     * @param chainId
     * @param txStr
     */
    public static boolean rollBackUnconfirmTx(int chainId, String txStr) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(AccountConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, AccountConstant.RPC_VERSION);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("tx", txStr);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "rollBackUnconfirmTx", params);
            if (!cmdResp.isSuccess()) {
                Log.error("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", ModuleE.LG.abbr, "rollBackUnconfirmTx", cmdResp.getResponseComment());
                throw new NulsException(AccountErrorCode.FAILED);
            }
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("rollBackUnconfirmTx");
            return (int) result.get("value") == 1;
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
            params.put(Constants.CHAIN_ID, chainId);
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
            Log.error("", e);
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
            params.put(Constants.CHAIN_ID, chainId);
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
            Log.error("", e);
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
            params.put(Constants.CHAIN_ID, chainId);
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
            Log.error("", e);
        }
        return null;
    }

}
