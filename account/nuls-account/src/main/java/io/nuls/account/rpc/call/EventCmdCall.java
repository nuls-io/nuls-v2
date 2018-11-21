package io.nuls.account.rpc.call;

import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.parse.JSONUtils;

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
            String response = CmdDispatcher.call("send", new Object[]{topic, "ac", data}, 1);
            CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
