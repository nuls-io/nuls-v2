package io.nuls.network.model.dto;

import io.nuls.network.manager.TimeManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lanjinsheng
 * @date 2019-07-16
 */
public class PeerMessage {
    String nodeId;
    String cmd;
    String messageBody;
    long createTime = 0;
    int tryTimes = 0;

    public PeerMessage(String nodeId, String cmd, String messageBody) {
        this.cmd = cmd;
        this.nodeId = nodeId;
        this.messageBody = messageBody;
        this.createTime = TimeManager.currentTimeMillis();
    }

    public Map<String, Object> toMap(int chainId) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("chainId", chainId);
        paramMap.put("nodeId", nodeId);
        paramMap.put("cmd", cmd);
        paramMap.put("messageBody", messageBody);
        return paramMap;
    }


    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getTryTimes() {
        return tryTimes;
    }

    public void setTryTimes(int tryTimes) {
        this.tryTimes = tryTimes;
    }
}
