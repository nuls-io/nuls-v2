package io.nuls.core.rpc.model;

import java.lang.annotation.*;

/**
 * @author: PierreLuo
 * @date: 2023/6/27
 * Mark Core ModulecmdModule
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NulsCoresCmd {
    ModuleE module();
}
