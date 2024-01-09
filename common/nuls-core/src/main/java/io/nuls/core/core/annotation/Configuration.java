package io.nuls.core.core.annotation;

import java.lang.annotation.*;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 19:35
 * @Description:
 * Configuration file annotations
 * After adding this annotation, configuration items will be injected by name
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Configuration {

    /**
     * save name
     * Storage Name
     * @return
     */
    String domain();


}
