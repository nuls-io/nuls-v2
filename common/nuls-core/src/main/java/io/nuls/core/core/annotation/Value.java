package io.nuls.core.core.annotation;

import java.lang.annotation.*;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 19:37
 * @Description:
 * Annotate on member variables and set the name of the corresponding configuration item
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {

    String value();

    /**
     * Non empty validation of configuration file
     */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface  NotNull {}
}
