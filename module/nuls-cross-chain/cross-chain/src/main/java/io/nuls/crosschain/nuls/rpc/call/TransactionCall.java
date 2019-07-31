package io.nuls.crosschain.nuls.rpc.call;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.nuls.model.bo.Chain;

import java.util.HashMap;
import java.util.Map;
/**
 * 与交易模块交互类
 * Interaction class with transaction module
 * @author tag
 * 2019/4/10
 */
public class TransactionCall {
    /**
     * 将新创建的交易发送给交易管理模块
     * The newly created transaction is sent to the transaction management module
     *
     * @param chain chain info
     * @param tx transaction hex
     */
    @SuppressWarnings("unchecked")
    public static boolean sendTx(Chain chain, String tx) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
        params.put("tx", tx);
        try {
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_newTx", params);
            if (!cmdResp.isSuccess()) {
                String errorCode = cmdResp.getResponseErrorCode();
                chain.getLogger().error("Call interface [{}] error, ErrorCode is {}, ResponseComment:{}",
                        "tx_newTx", errorCode, cmdResp.getResponseComment());
                throw new NulsException(ErrorCode.init(errorCode));
            }
            return true;
        }catch (NulsException e){
            throw e;
        }catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
    }
}
