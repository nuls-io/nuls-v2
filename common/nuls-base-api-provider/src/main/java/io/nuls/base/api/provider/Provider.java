package io.nuls.base.api.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 14:36
 * @Description: Function Description
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface Provider {

    public static enum ProviderType {
        RPC,
        Nulstar
    }

    ProviderType value() default ProviderType.RPC;

}
