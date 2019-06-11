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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TxDefine txDefine = (TxDefine) o;

        if (type != txDefine.type) {
            return false;
        }
        if (systemTx != txDefine.systemTx) {
            return false;
        }
        if (unlockTx != txDefine.unlockTx) {
            return false;
        }
        if (verifySignature != txDefine.verifySignature) {
            return false;
        }
        return handler != null ? handler.equals(txDefine.handler) : txDefine.handler == null;

    }

    @Override
    public int hashCode() {
        int result = (int) type;
        result = 31 * result + (systemTx ? 1 : 0);
        result = 31 * result + (unlockTx ? 1 : 0);
        result = 31 * result + (verifySignature ? 1 : 0);
        result = 31 * result + (handler != null ? handler.hashCode() : 0);
        return result;
    }
}
