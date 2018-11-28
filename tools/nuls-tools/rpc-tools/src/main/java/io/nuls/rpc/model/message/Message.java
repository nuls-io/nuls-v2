package io.nuls.rpc.model.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
@ToString
@NoArgsConstructor
public class Message {

    @Getter
    @Setter
    private String messageId;
    @Getter
    @Setter
    private String timestamp;
    @Getter
    @Setter
    private String timezone;
    @Getter
    @Setter
    private String messageType;
    @Getter
    @Setter
    private Object messageData;
}
