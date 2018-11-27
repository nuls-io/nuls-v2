package io.nuls.rpc.model.message;

import lombok.*;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Ack {
    @Getter
    @Setter
    private String requestId;
}
