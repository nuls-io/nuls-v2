package io.nuls.core.rpc.protocol;

/**
 * 交易注册类
 * Transaction registration class
 *
 * @author tag
 * 2018/11/30
 */
public class TxRegisterDetail {
    /**
     * 交易类型
     * Transaction type
     */
    private int txType;
    /**
     * 交易验证方法
     * Transaction verification method
     */
    private String validator;
    /**
     * 交易提交方法
     * Transaction submission method
     */
    private String commit;
    /**
     * 交易回滚方法
     * Transaction rollback method
     */
    private String rollback;
    /**
     * 是否是系统交易
     * Is it a system transaction
     */
    private boolean systemTx;
    /**
     * 是否是解锁交易
     * Is it a system transaction
     */
    private boolean unlockTx;
    /**
     * 交易是否需要签名
     * Is it a system transaction
     */
    private boolean verifySignature;

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
}
