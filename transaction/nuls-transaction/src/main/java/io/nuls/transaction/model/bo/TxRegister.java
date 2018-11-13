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
}
