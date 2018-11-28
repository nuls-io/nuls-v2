package io.nuls.rpc.model.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
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
