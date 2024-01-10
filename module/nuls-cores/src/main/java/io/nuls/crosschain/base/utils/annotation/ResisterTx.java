package io.nuls.crosschain.nuls.utils.annotation;

import io.nuls.crosschain.nuls.utils.enumeration.TxMethodType;
import io.nuls.crosschain.nuls.utils.enumeration.TxProperty;

import java.lang.annotation.*;

/**
 * This annotation is used to identify the method that needs to be registered with the transaction module
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
