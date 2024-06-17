package io.nuls.base.api.provider;

import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 19:46
 * @Description: rpc provider Basic class
 */
public abstract class BaseRpcService extends BaseService {

    public static final ErrorCode RPC_ERROR_CODE = ErrorCode.init("10016");

    /**
     * Call other modulesrpcinterface
     * @param module Module Name
     * @param method rpcInterface Name
     * @param req Business parameter object
     * @param callback Callback function
     * @param <T> Return value generic type
     * @return
     */
    protected <T,R> Result<T> callRpc(String module,String method,Object req,Function<R,Result> callback) {
        Map<String, Object> params = MapUtils.beanToLinkedMap(req);
        Log.debug("call {} rpc , method : {},param : {}",module,method,params);
        Response cmdResp = null;
        try {
            cmdResp = ResponseMessageProcessor.requestAndResponse(module, method, params);
            Log.debug("result : {}",cmdResp);
        } catch (Exception e) {
            Log.warn("Calling remote interface failed. module:{} - interface:{} - message:{}", module, method, e.getMessage());
            return fail(RPC_ERROR_CODE,e.getMessage());
        }
        if (!cmdResp.isSuccess()) {
            String comment = cmdResp.getResponseComment();
            if(StringUtils.isBlank(comment)) {
                comment = "";
            }
            Log.warn("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", module, method, cmdResp.getResponseComment());
            if(cmdResp.getResponseStatus() == Response.FAIL){
                //business error
                String errorCode = cmdResp.getResponseErrorCode();
                if(StringUtils.isBlank(errorCode)){
                    return fail(RPC_ERROR_CODE, StringUtils.isBlank(comment) ? "unknown error" : comment);
                }
                return fail(ErrorCode.init(errorCode), comment);
            }else{
                if(StringUtils.isNotBlank(comment)) {
                    return fail(RPC_ERROR_CODE, comment);
                }
                return fail(RPC_ERROR_CODE, "unknown error");
            }
        }
        return callback.apply((R) ((HashMap) cmdResp.getResponseData()).get(method));
    }

    protected abstract  <T,R> Result<T> call(String method, Object req, Function<R,Result> callback);

    protected Result<String> callReturnString(String method,Object req, String fieldName){
        Function<Map,Result> fun = res->{
            String data = (String) res.get(fieldName);
            return success(data);
        };
        return call(method,req,fun);
    }

    protected Result<Map> callResutlMap(String method,Object req){
        Function<Map,Result> fun = res->{
            return success(res);
        };
        return call(method,req,fun);
    }


}
