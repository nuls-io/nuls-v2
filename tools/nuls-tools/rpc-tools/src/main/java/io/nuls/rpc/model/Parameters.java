package io.nuls.rpc.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解类，Parameter的集合
 * Annotation classes, sets of Parameters
 *
 * @author tangyi
 * @date 2018/11/19
 * @description
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameters {
    Parameter[] value();
}
