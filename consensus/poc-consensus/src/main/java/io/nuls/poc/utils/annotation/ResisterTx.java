package io.nuls.poc.utils.annotation;

import io.nuls.poc.utils.enumeration.TxMethodType;
import io.nuls.poc.utils.enumeration.TxProperty;

import java.lang.annotation.*;

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
    TxProperty txType();
    TxMethodType methodType();
    String methodName();
}
