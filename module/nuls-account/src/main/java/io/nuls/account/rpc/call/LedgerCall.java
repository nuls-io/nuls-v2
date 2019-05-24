package io.nuls.account.rpc.call;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Chain;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 账本模块接口调用
 *
 * @author: qinyifeng
 * @date: 2018/12/12
 */
public class LedgerCall {

    /**
     * 查询账户余额
     */
    public static HashMap getBalanceNonce(Chain chain, int assetChainId, int assetId, String address) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            params.put("address", address);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBalanceNonce", params);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", ModuleE.LG.abbr, "getBalanceNonce", cmdResp.getResponseComment());
                throw new NulsException(AccountErrorCode.FAILED);
            }
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("getBalanceNonce");
            return result;
        } catch (Exception e) {
            String msg = MessageFormat.format("Calling remote interface failed. module:{0} - interface:{1}", ModuleE.LG.abbr, "getBalanceNonce");
            chain.getLogger().error(msg, e);
            return null;
        }
    }

    /**
     * 查询账户余额
     */
    public static BigInteger getBalance(Chain chain, int assetChainId, int assetId, String address) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("address", address);
            params.put("assetChainId", assetChainId);
            params.put("assetId", assetId);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBalance", params);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", ModuleE.LG.abbr, "getBalance", cmdResp.getResponseComment());
                throw new NulsException(AccountErrorCode.FAILED);
            }
            HashMap result = (HashMap) ((HashMap) cmdResp.getResponseData()).get("getBalance");
            if(null == result){
                chain.getLogger().error("call getBalance response data is null, error:{}",
                        AccountErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
                return new BigInteger("0");
            }
            Object available = result.get("available");
            if(null == available){
                chain.getLogger().error("call getBalance response available is null, error:{}",
                        AccountErrorCode.REMOTE_RESPONSE_DATA_NOT_FOUND.getCode());
                return new BigInteger("0");
            }
            return BigIntegerUtils.stringToBigInteger(String.valueOf(available));
        } catch (Exception e) {
            String msg = MessageFormat.format("Calling remote interface failed. module:{0} - interface:{1}", ModuleE.LG.abbr, "getBalance");
            chain.getLogger().error(msg, e);
            return new BigInteger("0");
        }
    }

}
