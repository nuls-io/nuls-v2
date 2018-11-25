package io.nuls.rpc.model.message;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
public class Notification {
    private String notificationAck;
    private String notificationType;
    private String notificationComment;
    private String notificationData;

    public String getNotificationAck() {
        return notificationAck;
    }

    public void setNotificationAck(String notificationAck) {
        this.notificationAck = notificationAck;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getNotificationComment() {
        return notificationComment;
    }

    public void setNotificationComment(String notificationComment) {
        this.notificationComment = notificationComment;
    }

    public String getNotificationData() {
        return notificationData;
    }

    public void setNotificationData(String notificationData) {
        this.notificationData = notificationData;
    }
}
