package io.nuls.base.api.provider.transaction.facade;

/**
 * @Author: zhoulijun
 * @Time: 2019-07-18 15:18
 * @Description: Create multiple transaction return objects
 */
public class MultiSignTransferRes {

    private String tx;

    private String txHash;

    private boolean completed;

    public String getTx() {
        return tx;
    }

    public void setTx(String tx) {
        this.tx = tx;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
