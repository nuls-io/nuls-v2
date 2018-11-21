package io.nuls.account.rpc.call;

import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdResponse;
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
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("topic", topic);
            params.put("data", data);
            String response = CmdDispatcher.request("send", params);
            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
