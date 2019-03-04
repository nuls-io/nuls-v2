package io.nuls.api.rpc;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
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

    /**
     * 调用其他模块接口
     * Call other module interfaces
     */
    public static Object request(String moduleCode, String cmd, Map params) throws NulsException {
        try {
            params.put(Constants.VERSION_KEY_STR, "1.0");
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(moduleCode, cmd, params);
            Map resData = (Map)cmdResp.getResponseData();
            if (!cmdResp.isSuccess()) {
                String errorMsg = null;
                if(null == resData){
                    errorMsg = String.format("Remote call fail. ResponseComment: %s ", cmdResp.getResponseComment());
                }else {
                    Map map = (Map) resData.get(cmd);
                    errorMsg = String.format("Remote call fail. msg: %s - code: %s - module: %s - interface: %s \n- params: %s ",
                            map.get("msg"), map.get("code"), moduleCode, cmd, JSONUtils.obj2PrettyJson(params));
                }
                throw new Exception(errorMsg);
            }
            /*if (null == resData) {
                return null;
            }*/
            return resData.get(cmd);
        } catch (Exception e) {
            Log.debug("cmd: {}", cmd);
            throw new NulsException(e);
        }
    }


}
