package io.nuls.rpc.model;

import java.lang.annotation.*;

/**
 * 注解类，用以描述对外提供接口的参数信息
 * Annotation classes to describe parameter information that provides interfaces to the outside world
 *
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
