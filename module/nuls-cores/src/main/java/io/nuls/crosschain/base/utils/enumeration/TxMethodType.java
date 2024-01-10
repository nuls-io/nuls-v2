package io.nuls.crosschain.nuls.utils.enumeration;
/**
 * This enumeration class identifies the type of transaction method registered with the transaction module
 * This enumeration class identifies the type of transaction method registered with the transaction module.
 * @author  tag
 * 2018/11/30
 * */
public enum TxMethodType {
    /**
     * validate
     * validate
     * */
    VALID,
    /**
     * Submit
     * commit
     * */
    COMMIT,
    /**
     * RollBACK
     * rollback
     * */
    ROLLBACK
}
