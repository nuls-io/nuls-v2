package io.nuls.rpc.model;

import lombok.*;

/**
 * @author tangyi
 * @date 2018/11/19
 * @description
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
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
