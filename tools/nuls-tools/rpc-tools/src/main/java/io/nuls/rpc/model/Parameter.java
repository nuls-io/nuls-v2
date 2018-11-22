package io.nuls.rpc.model;

import java.lang.annotation.*;

/**
 * @author tangyi
 * @date 2018/11/19
 * @description
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Parameters.class)
public @interface Parameter {
    String parameterName();

    String parameterType();

    String parameterValidRange() default "";

    String parameterValidRegExp() default "";
}
