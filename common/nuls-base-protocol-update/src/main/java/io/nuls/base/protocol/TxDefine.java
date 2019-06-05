package io.nuls.base.protocol;

public class TxDefine {
    private short type;
    private boolean systemTx;
    private boolean unlockTx;
    private boolean verifySignature;
    private boolean verifyFee;
    private String handler;

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
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

    public boolean getVerifyFee() {
        return verifyFee;
    }

    public void setVerifyFee(boolean verifyFee) {
        this.verifyFee = verifyFee;
    }

    @Override
    public String toString() {
        return "TxDefine{" +
                "type=" + type +
                ", systemTx=" + systemTx +
                ", unlockTx=" + unlockTx +
                ", verifySignature=" + verifySignature +
                ", verifyFee=" + verifyFee +
                ", handler='" + handler + '\'' +
                '}';
    }
}
