package io.nuls.rpc.model.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 通知
 * Notification
 *
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
@ToString
@NoArgsConstructor
public class Notification {
    @Getter
    @Setter
    private String notificationAck;
    @Getter
    @Setter
    private String notificationType;
    @Getter
    @Setter
    private String notificationComment;
    @Getter
    @Setter
    private String notificationData;
}
