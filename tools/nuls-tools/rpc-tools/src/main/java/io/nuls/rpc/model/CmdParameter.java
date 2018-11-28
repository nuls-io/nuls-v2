package io.nuls.rpc.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author tangyi
 * @date 2018/11/19
 * @description
 */
@ToString
@NoArgsConstructor
public class CmdParameter {
    @Getter
    @Setter
    private String parameterName;
    @Getter
    @Setter
    private String parameterType;
    @Getter
    @Setter
    private String parameterValidRange;
    @Getter
    @Setter
    private String parameterValidRegExp;
}
