package io.nuls.core.core.annotation;

import java.lang.annotation.*;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-04 13:34
 * @Description: signbeanThe loading order and implementation ofInitializingBeanInterface class executionafterPropertiesSetOrder of
 * If this annotation is not set, load the weight value as1
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Order {

    public static final int DEFALUT_ORDER = 1;

    int value() default 1;

}
