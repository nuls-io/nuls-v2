package io.nuls.base.data.protocol;

import java.util.List;

public class Protocol {

    private short version;
    private List<TransactionConfig> allowTx;
    private List<MessageConfig> allowMsg;

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public List<TransactionConfig> getAllowTx() {
        return allowTx;
    }

    public void setAllowTx(List<TransactionConfig> allowTx) {
        this.allowTx = allowTx;
    }

    public List<MessageConfig> getAllowMsg() {
        return allowMsg;
    }

    public void setAllowMsg(List<MessageConfig> allowMsg) {
        this.allowMsg = allowMsg;
    }

    public Protocol() {
    }
}
