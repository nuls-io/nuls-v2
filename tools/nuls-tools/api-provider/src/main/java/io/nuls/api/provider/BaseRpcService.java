package io.nuls.api.provider;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.parse.MapUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 19:46
 * @Description: rpc provider 基础类
 */
@Slf4j
public abstract class BaseRpcService extends BaseService {

    /**
     * 调用其他模块rpc接口
     * @param module 模块名称
     * @param method rpc接口名称
     * @param req 业务参数对象
     * @param callback 回调函数
     * @param <T> 返回值泛型类型
     * @return
     */
    protected  <T> Result<T> callRpc(String module,String method, Object req, Function<Map,Result> callback) {
        Map<String, Object> params = MapUtils.beanToLinkedMap(req);
        params.put(Constants.VERSION_KEY_STR, "1.0");
        log.debug("call {} rpc , method : {},param : {}",module,method,params);
        Response cmdResp = null;
        try {
            cmdResp = ResponseMessageProcessor.requestAndResponse(module, method, params);
            log.debug("result : {}",cmdResp);
        } catch (Exception e) {
            log.error("Calling remote interface failed. module:{} - interface:{} - message:{}", module, method, e.getMessage());
            return fail(BaseService.ERROR_CODE, ErrorCode.init(BaseService.ERROR_CODE).getMsg());
        }
        if (!cmdResp.isSuccess()) {
            log.error("Calling remote interface failed. module:{} - interface:{} - ResponseComment:{}", module, method, cmdResp.getResponseComment());
            return fail(ERROR_CODE, cmdResp.getResponseComment());
        }
        return callback.apply((Map) ((HashMap) cmdResp.getResponseData()).get(method));
    }

}
