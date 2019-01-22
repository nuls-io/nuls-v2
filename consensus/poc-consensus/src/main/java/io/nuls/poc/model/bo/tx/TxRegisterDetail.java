package io.nuls.poc.model.bo.tx;

import io.nuls.poc.utils.enumeration.TxProperty;

/**
 * 交易注册类
 * Transaction registration class
 *
 * @author tag
 * 2018/11/30
 * */
public class TxRegisterDetail {
    /**
    * 交易类型
    * Transaction type
    * */
    private int txType;
    /**
    * 交易验证方法
    * Transaction verification method
    * */
    private String validateCmd;
    /**
    * 交易提交方法
    * Transaction submission method
    * */
    private String commitCmd;
    /**
    * 交易回滚方法
    * Transaction rollback method
    * */
    private String rollbackCmd;
    /**
     * 是否是系统交易
     * Is it a system transaction
     * */
    private boolean systemTx;
    /**
     * 是否是解锁交易
     * Is it a system transaction
     * */
    private boolean unlockTx;
    /**
     * 交易是否需要签名
     * Is it a system transaction
     * */
    private boolean verifySignature;

    public TxRegisterDetail(int txType){
        this.txType = txType;
    }

    public TxRegisterDetail(TxProperty txProperty){
        this.txType = txProperty.txType;
        this.systemTx = txProperty.systemTx;
        this.unlockTx = txProperty.unlockTx;
        this.verifySignature = txProperty.verifySignature;
        this.validateCmd = "";
        this.validateCmd = "";
        this.commitCmd = "";
    }

    public int getTxType() {
        return txType;
    }

    public void setTxType(int txType) {
        this.txType = txType;
    }

    public String getValidateCmd() {
        return validateCmd;
    }

    public void setValidateCmd(String validateCmd) {
        this.validateCmd = validateCmd;
    }

    public String getCommitCmd() {
        return commitCmd;
    }

    public void setCommitCmd(String commitCmd) {
        this.commitCmd = commitCmd;
    }

    public String getRollbackCmd() {
        return rollbackCmd;
    }

    public void setRollbackCmd(String rollbackCmd) {
        this.rollbackCmd = rollbackCmd;
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
