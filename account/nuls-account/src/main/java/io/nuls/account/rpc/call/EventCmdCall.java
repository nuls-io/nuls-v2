package io.nuls.account.rpc.call;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.RpcConstant;
import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.parse.JSONUtils;

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
            int messageId = CmdDispatcher.request(RpcConstant.EVENT_SEND_CMD, params);
            String response = CmdDispatcher.getResponse(messageId);
            Response cmdResp = JSONUtils.json2pojo(response, Response.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
