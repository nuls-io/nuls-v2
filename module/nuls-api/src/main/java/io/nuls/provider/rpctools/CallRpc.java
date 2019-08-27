package io.nuls.provider.rpctools;

import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 14:09
 * @Description: rpc工具类
 */
public interface CallRpc {

    default  <T,R> R callRpc(String module, String method, Map<String, Object> params, Function<T, R> callback) {
        Log.debug("call {} rpc , method : {},param : {}",module,method,params);
        Response cmdResp = null;
        try {
            cmdResp = ResponseMessageProcessor.requestAndResponse(module, method, params);
            Log.debug("result : {}",cmdResp);
        } catch (Exception e) {
            Log.warn("Calling remote interface failed. module:{} - interface:{} - message:{}", module, method, e.getMessage());
            throw new NulsRuntimeException(CommonCodeConstanst.FAILED, e.getMessage());
        }
        if (!cmdResp.isSuccess()) {
            String comment = cmdResp.getResponseComment();
            if(StringUtils.isBlank(comment)) {
                comment = "";
            }
            Log.warn("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", module, method, comment);
            if(cmdResp.getResponseStatus() == Response.FAIL){
                //business error
                String errorCode = cmdResp.getResponseErrorCode();
                if(StringUtils.isBlank(errorCode)){
                    throw new NulsRuntimeException(CommonCodeConstanst.SYS_UNKOWN_EXCEPTION, comment);
                }
                throw new NulsRuntimeException(ErrorCode.init(errorCode), comment);
            }else{
                if(StringUtils.isNotBlank(comment)) {
                    throw new NulsRuntimeException(CommonCodeConstanst.FAILED, comment);
                }
                throw new NulsRuntimeException(CommonCodeConstanst.SYS_UNKOWN_EXCEPTION, "unknown error");
            }
        }
        return callback.apply((T) ((HashMap) cmdResp.getResponseData()).get(method));
    }

}
