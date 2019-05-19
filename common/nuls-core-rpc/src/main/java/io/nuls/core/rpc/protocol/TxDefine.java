package io.nuls.core.rpc.protocol;

public class TxDefine {
    private short type;
    private boolean systemTx;
    private boolean unlockTx;
    private boolean verifySignature;
    private String handler;
    private String validate;
    private String commit;
    private String rollback;

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

    public String getValidate() {
        return validate;
    }

    public void setValidate(String validate) {
        this.validate = validate;
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

    public TxDefine() {
    }

    @Override
    public String toString() {
        return "TxDefine{" +
                "type=" + type +
                ", systemTx=" + systemTx +
                ", unlockTx=" + unlockTx +
                ", verifySignature=" + verifySignature +
                ", handler='" + handler + '\'' +
                ", validate='" + validate + '\'' +
                ", commit='" + commit + '\'' +
                ", rollback='" + rollback + '\'' +
                '}';
    }
}
