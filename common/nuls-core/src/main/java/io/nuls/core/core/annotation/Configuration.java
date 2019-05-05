package io.nuls.core.core.annotation;

import java.lang.annotation.*;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 19:35
 * @Description:
 * 配置文件注解
 * 加入此注解后，会按名字注入配置项
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Configuration {

    /**
     * save name
     * 存储名称
     * @return
     */
    String domain();


}