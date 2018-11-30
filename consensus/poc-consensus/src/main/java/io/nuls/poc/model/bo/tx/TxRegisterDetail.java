package io.nuls.poc.model.bo.tx;
/**
 * 交易注册类
 * @author tag
 * 2018/11/30
 * */
public class TxRegisterDetail {
    private int txType;          //交易类型
    private String validateCmd;  //交易验证方法
    private String commitCmd;    //交易提交方法
    private String rollbackCmd;  //交易回滚方法

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
