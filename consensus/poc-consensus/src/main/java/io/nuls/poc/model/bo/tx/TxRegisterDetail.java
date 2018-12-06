package io.nuls.poc.model.bo.tx;
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

    public TxRegisterDetail(int txType){
        this.txType = txType;
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
}
