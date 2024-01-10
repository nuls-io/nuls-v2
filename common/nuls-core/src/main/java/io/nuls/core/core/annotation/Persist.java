package io.nuls.core.core.annotation;

import java.lang.annotation.*;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-15 16:51
 * @Description:
 *   Configuration items can be persisted
 *   can save config item
 */
@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Persist {

}
