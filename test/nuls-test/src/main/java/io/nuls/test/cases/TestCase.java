package io.nuls.test.cases;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-19 20:49
 * @Description: Automatically initiated test cases
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TestCase {

    String value();

}
