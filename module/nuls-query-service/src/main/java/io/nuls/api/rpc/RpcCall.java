package io.nuls.api.rpc;

import io.nuls.api.constant.ApiErrorCode;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;

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
            Response response;
            try {
                if(null == timeout) {
                    response = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params);
                }else{
                    response = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params, timeout);
                }
            } catch (Exception e) {
                LoggerUtil.commonLog.error(e);
                throw new NulsException(ApiErrorCode.SYS_UNKOWN_EXCEPTION, e.getMessage());
            }
            if (!response.isSuccess()) {
                String comment = response.getResponseComment();
                if(StringUtils.isBlank(comment)) {
                    comment = "";
                }

                String errorCode = response.getResponseErrorCode();
                LoggerUtil.commonLog.error("Call interface [{}] error, ErrorCode is {}, ResponseComment:{}", cmd, errorCode, response.getResponseComment());
                if(response.getResponseStatus() == Response.FAIL){
                    //business error
                    if(StringUtils.isBlank(errorCode)){
                        throw new NulsException(CommonCodeConstanst.SYS_UNKOWN_EXCEPTION, comment);
                    }
                    throw new NulsException(ErrorCode.init(errorCode), comment);
                }else{
                    if(StringUtils.isNotBlank(comment)) {
                        throw new NulsException(CommonCodeConstanst.FAILED, comment);
                    }
                    throw new NulsException(CommonCodeConstanst.SYS_UNKOWN_EXCEPTION, "unknown error");
                }
            }
            Map data = (Map)response.getResponseData();
            return data.get(cmd);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            if(e instanceof NulsException) {
                throw (NulsException) e;
            }
            throw new NulsException(ApiErrorCode.SYS_UNKOWN_EXCEPTION, e.getMessage());
        }
    }

    public static Response requestAndResponse(String moduleCode, String cmd, Map params) throws NulsException {
        try {
            Response response;
            try {
                response = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params);
            } catch (Exception e) {
                LoggerUtil.commonLog.error(e);
                throw new NulsException(ApiErrorCode.SYS_UNKOWN_EXCEPTION);
            }
            if (!response.isSuccess()) {
                String errorCode = response.getResponseErrorCode();
                LoggerUtil.commonLog.error("Call interface [{}] error, ErrorCode is {}, ResponseComment:{}", cmd, errorCode, response.getResponseComment());
            }
            return response;
        } catch (RuntimeException e) {
            LoggerUtil.commonLog.error(e);
            throw new NulsException(ApiErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }
}
