package io.nuls.rpc.model.message;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
public class Message {

    private int messageId;
    private long timestamp;
    private long timezone;
    private String messageType;
    private Object messageData;

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimezone() {
        return timezone;
    }

    public void setTimezone(long timezone) {
        this.timezone = timezone;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Object getMessageData() {
        return messageData;
    }

    public void setMessageData(Object messageData) {
        this.messageData = messageData;
    }
}
