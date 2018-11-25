package io.nuls.rpc.model.message;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
public class Message {

    private String messageId;
    private String timestamp;
    private String timezone;
    private String messageType;
    private Object messageData;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
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
