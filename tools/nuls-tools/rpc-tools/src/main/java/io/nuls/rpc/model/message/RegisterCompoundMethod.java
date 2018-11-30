package io.nuls.rpc.model.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 订阅多个远程方法
 * Subscribe to multiple remote methods
 *
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
@ToString
@NoArgsConstructor
public class RegisterCompoundMethod {
    @Getter
    @Setter
    private String compoundMethodName;
    @Getter
    @Setter
    private String compoundMethodDescription;
    @Getter
    @Setter
    private List<Object> compoundMethods;
}
