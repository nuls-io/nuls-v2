package io.nuls.core.rpc.model;

import java.lang.annotation.*;

/**
 * @author: PierreLuo
 * @date: 2023/6/27
 * 标记核心模块cmd所属模块
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NulsCoresCmd {
    ModuleE module();
}
