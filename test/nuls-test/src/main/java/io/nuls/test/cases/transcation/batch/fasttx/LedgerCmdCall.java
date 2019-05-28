package io.nuls.test.cases.transcation.batch.fasttx;

import io.nuls.base.RPCUtil;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

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
                throw new NulsException(CommonCodeConstanst.FAILED);
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
