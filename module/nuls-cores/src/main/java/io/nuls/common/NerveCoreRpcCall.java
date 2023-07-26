package io.nuls.common;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;

import java.util.Map;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * 调用其他模块跟交易相关的接口
 */
public class NerveCoreRpcCall {


    public static Object requestAndResponse(String moduleCode, String cmd, Map params) throws NulsException {
        return requestAndResponse(moduleCode, cmd, params, null);
    }

    /**
     * 调用其他模块接口
     * Call other module interfaces
     */
    public static Object requestAndResponse(String moduleCode, String cmd, Map params, Long timeout) throws NulsException {
        try {
            params.put(Constants.VERSION_KEY_STR, TxConstant.RPC_VERSION);
            Response response = null;
            try {
                if (null == timeout) {
                    response = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params);
                } else {
                    response = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params, timeout);
                }
            } catch (Exception e) {
                LOG.error("Call interface [{}] error, moduleCode:{}, ", moduleCode, cmd);
                LOG.error(e);
                throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
            }
            if (!response.isSuccess()) {
                String errorCode = response.getResponseErrorCode();
                LOG.error("Call interface [{}] error, moduleCode:{}, ErrorCode is {}, ResponseComment:{}", moduleCode, cmd, errorCode, response.getResponseComment());
                throw new NulsException(ErrorCode.init(errorCode));
            }
            Map data = (Map) response.getResponseData();
            return data.get(cmd);
        } catch (RuntimeException e) {
            LOG.error(e);
            throw new NulsException(TxErrorCode.RPC_REQUEST_FAILD);
        }
    }

}
