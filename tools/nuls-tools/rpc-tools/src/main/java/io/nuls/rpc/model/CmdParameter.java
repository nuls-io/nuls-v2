package io.nuls.rpc.model;

import lombok.*;

/**
 * 对外提供的方法的参数信息
 * Parameter information of methods provided to the outside world
 *
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
