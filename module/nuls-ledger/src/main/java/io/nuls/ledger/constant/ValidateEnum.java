package io.nuls.ledger.constant;

/**
 * @author lanjinsheng
 */

public enum ValidateEnum {
    /**
     * 校验成功
     */
    SUCCESS_CODE(1),
    /**
     * 孤儿
     */
    ORPHAN_CODE(2),
    /**
     * 双花
     */
    DOUBLE_EXPENSES_CODE(3),
    /**
     * 失败
     */
    FAIL_CODE(4),
    /**
     * 交易已存在
     */
    TX_EXIST_CODE(5);
    private int value;

    private ValidateEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
