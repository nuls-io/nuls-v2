package io.nuls.rpc.model.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
@ToString
@NoArgsConstructor
public class Request {
    @Getter
    @Setter
    private String requestAck;
    @Getter
    @Setter
    private String subscriptionEventCounter;
    @Getter
    @Setter
    private String subscriptionPeriod;
    @Getter
    @Setter
    private String subscriptionRange;
    @Getter
    @Setter
    private String responseMaxSize;
    @Getter
    @Setter
    private Map<String,Object> requestMethods;
}
