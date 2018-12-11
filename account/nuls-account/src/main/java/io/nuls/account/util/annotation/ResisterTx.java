package io.nuls.account.util.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用来标识需要向交易模块注册的方法
 * This annotation identifies the method that needs to be registered with the transaction module
 * @author tag
 * 2018/11/30
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResisterTx {
    int txType();
    TxMethodType methodType();
    String methodName();
}
