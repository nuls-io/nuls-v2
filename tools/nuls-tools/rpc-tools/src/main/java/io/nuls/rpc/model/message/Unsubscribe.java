package io.nuls.rpc.model.message;

import lombok.*;

/**
 * 取消订阅
 * unsubscribe
 *
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
@ToString
@NoArgsConstructor
public class Unsubscribe {
    @Getter
    @Setter
    private String[] unsubscribeMethods;
}
