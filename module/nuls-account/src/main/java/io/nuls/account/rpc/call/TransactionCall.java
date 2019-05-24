package io.nuls.account.rpc.call;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.RpcConstant;
import io.nuls.account.model.bo.Chain;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: qinyifeng
 * @description: 交易模块接口调用
 * @date: 2018/11/27
 */
public class TransactionCall {

    /**
     * 发起新交易
     */
    public static boolean newTx(Chain chain, String txStr) {
        try {
            Map<String, Object> params = new HashMap<>(AccountConstant.INIT_CAPACITY_8);
            params.put(Constants.VERSION_KEY_STR, RpcConstant.TX_NEW_VERSION);
            params.put(RpcConstant.TX_CHAIN_ID, chain.getChainId());
            params.put(RpcConstant.TX_DATA, txStr);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, RpcConstant.TX_NEW_CMD, params);
            return cmdResp.isSuccess();
        } catch (Exception e) {
            String msg = MessageFormat.format("Calling remote interface failed. module:{0} - interface:{1}", ModuleE.TX.abbr, RpcConstant.TX_NEW_CMD);
            chain.getLogger().error(msg, e);
            return false;
        }
    }

}
