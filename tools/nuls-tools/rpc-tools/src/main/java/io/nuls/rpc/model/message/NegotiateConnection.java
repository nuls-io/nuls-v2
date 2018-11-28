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
public class NegotiateConnection {
    @Getter
    @Setter
    private String protocolVersion;
    @Getter
    @Setter
    private String compressionAlgorithm;
    @Getter
    @Setter
    private String compressionRate;
}
