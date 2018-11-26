package io.nuls.transaction.model.bo;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
public class TxRegister {

    /**
     * Transaction type
     */
    private int txType;

    /**
     * Transaction validator cmd name
     */
    private String validator;

    /**
     * Transaction commit cmd name
     */
    private String commit;

    /**
     * Transaction rollback cmd name
     */
    private String rollback;


    /**
     * 是否是系统产生的交易（打包节点产生，用于出块奖励结算、红黄牌惩罚），该种类型的交易在验证块大小时不计算在内，该类型交易不需要手续费
     * Is a system to produce trading (packaged node generation, for the piece reward settlement, CARDS punishment),
     * trading in the validation of this kind of new type block size is not taken into account, the types of transactions do not need poundage
     */
    private boolean systemTx;


    /**
     * 是否是解锁交易
     * If it's an unlocking transaction
     */
    public boolean unlockTx;

    /**
     * 该交易是否需要在账本中验证签名，所有系统产生的交易和一些特殊交易，不需要安装普通交易的方式验证签名，会提供额外的逻辑进行验证。
     * If the deal need to verify the signature in the book, all transactions system and some special deal,
     * no need to install the ordinary transaction way to verify the signature, will provide additional validation logic.
     */
    public boolean verifySignature;

    /**
     * module code
     */
    private String moduleValidator;

    /**
     * Module unified validator cmd name
     */
    private String moduleCode;

    public int getTxType() {
        return txType;
    }

    public void setTxType(int txType) {
        this.txType = txType;
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getRollback() {
        return rollback;
    }

    public void setRollback(String rollback) {
        this.rollback = rollback;
    }

    public boolean isSystemTx() {
        return systemTx;
    }

    public void setSystemTx(boolean systemTx) {
        this.systemTx = systemTx;
    }

    public boolean isUnlockTx() {
        return unlockTx;
    }

    public void setUnlockTx(boolean unlockTx) {
        this.unlockTx = unlockTx;
    }

    public boolean isVerifySignature() {
        return verifySignature;
    }

    public void setVerifySignature(boolean verifySignature) {
        this.verifySignature = verifySignature;
    }

    public String getModuleValidator() {
        return moduleValidator;
    }

    public void setModuleValidator(String moduleValidator) {
        this.moduleValidator = moduleValidator;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }
}
