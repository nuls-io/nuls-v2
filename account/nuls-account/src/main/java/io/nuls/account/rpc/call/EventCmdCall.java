package io.nuls.account.rpc.call;

import io.nuls.account.constant.RpcConstant;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/20
 */
public class EventCmdCall {

    /**
     * 事件发送
     *
     * @param topic
     * @param data
     */
    public static void sendEvent(String topic, String data) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, RpcConstant.EVENT_SEND_VERSION);
            params.put(RpcConstant.EVENT_SEND_TOPIC, topic);
            params.put(RpcConstant.EVENT_SEND_DATA, data);
            //Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.EB.abbr, RpcConstant.EVENT_SEND_CMD, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
