package io.nuls.base.data.protocol;

import java.util.List;

public class ProtocolConfigJson {

    private short version;
    private short extend;
    private List<TransactionConfig> addtx;
    private List<MessageConfig> addmsg;
    private List<ListItem> discardtx;
    private List<ListItem> discardmsg;

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public short getExtend() {
        return extend;
    }

    public void setExtend(short extend) {
        this.extend = extend;
    }

    public List<TransactionConfig> getAddtx() {
        return addtx;
    }

    public void setAddtx(List<TransactionConfig> addtx) {
        this.addtx = addtx;
    }

    public List<MessageConfig> getAddmsg() {
        return addmsg;
    }

    public void setAddmsg(List<MessageConfig> addmsg) {
        this.addmsg = addmsg;
    }

    public List<ListItem> getDiscardtx() {
        return discardtx;
    }

    public void setDiscardtx(List<ListItem> discardtx) {
        this.discardtx = discardtx;
    }

    public List<ListItem> getDiscardmsg() {
        return discardmsg;
    }

    public void setDiscardmsg(List<ListItem> discardmsg) {
        this.discardmsg = discardmsg;
    }

    public ProtocolConfigJson() {
    }
}
