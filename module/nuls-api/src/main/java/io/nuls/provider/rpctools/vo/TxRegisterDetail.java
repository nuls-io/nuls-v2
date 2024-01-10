package io.nuls.provider.rpctools.vo;

/**
 * Transaction registration class
 * Transaction registration class
 *
 * @author tag
 * 2018/11/30
 */
public class TxRegisterDetail {
    /**
     * Transaction type
     * Transaction type
     */
    private int txType;
    /**
     * Is it a system transaction
     * Is it a system transaction
     */
    private boolean systemTx;
    /**
     * Is it an unlocked transaction
     * Is it a system transaction
     */
    private boolean unlockTx;
    /**
     * Do transactions require signatures
     * Is it a system transaction
     */
    private boolean verifySignature;

    private boolean verifyFee;

    public int getTxType() {
        return txType;
    }

    public void setTxType(int txType) {
        this.txType = txType;
    }

    public boolean getSystemTx() {
        return systemTx;
    }

    public void setSystemTx(boolean systemTx) {
        this.systemTx = systemTx;
    }

    public boolean getUnlockTx() {
        return unlockTx;
    }

    public void setUnlockTx(boolean unlockTx) {
        this.unlockTx = unlockTx;
    }

    public boolean getVerifySignature() {
        return verifySignature;
    }

    public void setVerifySignature(boolean verifySignature) {
        this.verifySignature = verifySignature;
    }

    public boolean getVerifyFee() {
        return verifyFee;
    }

    public void setVerifyFee(boolean verifyFee) {
        this.verifyFee = verifyFee;
    }

    @Override
    public String toString() {
        return "TxRegisterDetail{" +
                "txType=" + txType +
                ", systemTx=" + systemTx +
                ", unlockTx=" + unlockTx +
                ", verifySignature=" + verifySignature +
                ", verifyFee=" + verifyFee +
                '}';
    }
}
