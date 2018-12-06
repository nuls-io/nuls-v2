package io.nuls.poc.utils.enumeration;
/**
 * 该枚举类标识的是注册到交易模块的交易方法类型
 * This enumeration class identifies the type of transaction method registered with the transaction module.
 * @author  tag
 * 2018/11/30
 * */
public enum TxMethodType {
    /**
     * 验证
     * validate
     * */
    VALID,
    /**
     * 提交
     * commit
     * */
    COMMIT,
    /**
     * 回滚
     * rollback
     * */
    ROLLBACK
}
