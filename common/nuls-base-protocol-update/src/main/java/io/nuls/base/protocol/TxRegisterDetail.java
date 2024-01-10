package io.nuls.base.protocol;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

/**
 * Transaction registration class
 * Transaction registration class
 *
 */
@ApiModel(description = "Transaction registration")
public class TxRegisterDetail {
    /**
     * Transaction type
     * Transaction type
     */
    @ApiModelProperty(description = "Transaction type")
    private int txType;
    /**
     * Is it a system transaction
     * Is it a system transaction
     */
    @ApiModelProperty(description = "Is it a system transaction")
    private boolean systemTx;
    /**
     * Is it an unlocked transaction
     * Is it a unlock transaction
     */
    @ApiModelProperty(description = "Is it an unlocked transaction")
    private boolean unlockTx;
    /**
     * Do transactions require signatures
     * Is it a sign-required transaction
     */
    @ApiModelProperty(description = "Do transactions require signatures")
    private boolean verifySignature;

    /**
     * Do transactions require verification fees
     * Is it a fee-validate-required transaction
     */
    @ApiModelProperty(description = "Do transactions require verification fees")
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
