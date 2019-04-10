package io.nuls.api.model.rpc;

public class FreezeInfo {

    private String txHash;

    private int type;

    private long time;

    private long lockedValue;

    private String amount;

    private String reason;

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getLockedValue() {
        return lockedValue;
    }

    public void setLockedValue(long lockedValue) {
        this.lockedValue = lockedValue;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
