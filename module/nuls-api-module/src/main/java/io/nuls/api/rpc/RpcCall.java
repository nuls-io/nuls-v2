package io.nuls.api.rpc;

import io.nuls.api.constant.ApiErrorCode;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.util.Map;

/**
 * 调用其他模块跟交易相关的接口
 *
 * @author: qinyifeng
 * @date: 2018/12/05
 */
public class RpcCall {

    public static Object request(String moduleCode, String cmd, Map params) throws NulsException {
        return request(moduleCode, cmd, params, null);
    }
    /**
     * 调用其他模块接口
     * Call other module interfaces
     */
    public static Object request(String moduleCode, String cmd, Map params, Long timeout) throws NulsException {
        try {
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            Response response = null;
            try {
                if(null == timeout) {
                    response = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params);
                }else{
                    response = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params, timeout);
                }
            } catch (Exception e) {
                Log.error(e);
                throw new NulsException(ApiErrorCode.SYS_UNKOWN_EXCEPTION);
            }
            if (!response.isSuccess()) {
                String errorCode = response.getResponseErrorCode();
                Log.error("Call interface [{}] error, ErrorCode is {}, ResponseComment:{}", cmd, errorCode, response.getResponseComment());
                throw new NulsException(ErrorCode.init(errorCode));
            }
            Map data = (Map)response.getResponseData();
            return data.get(cmd);
        } catch (RuntimeException e) {
            Log.error(e);
            throw new NulsException(ApiErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }
}
