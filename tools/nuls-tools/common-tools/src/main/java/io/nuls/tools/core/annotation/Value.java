package io.nuls.tools.core.annotation;

import java.lang.annotation.*;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 19:37
 * @Description:
 * 注解在成员变量上，会将对应的配置项注入此成员变量
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {

    String value();

}
