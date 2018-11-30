package io.nuls.poc.utils.enumeration;
/**
 * 该枚举类标识的是注册到交易模块的交易方法类型
 * This enumeration class identifies the type of transaction method registered with the transaction module.
 * @author  tag
 * 2018/11/30
 * */
public enum TxMethodType {
    VALID,     //验证
    COMMIT,    //提交
    ROLLBACK   //回滚
}
