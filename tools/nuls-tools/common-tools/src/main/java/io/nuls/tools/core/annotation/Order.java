package io.nuls.tools.core.annotation;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-04 13:34
 * @Description: 标记bean的加载顺序，以及实现了InitializingBean接口类执行afterPropertiesSet的顺序
 * 如果不设置此注解，加载权重值为1
 */
public @interface Order {

    public static final int DEFALUT_ORDER = 1;

    int value() default 1;

}
