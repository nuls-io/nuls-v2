package io.nuls.core.rpc.protocol;

import java.util.List;

public class ProtocolConfigJson {

    private short version;
    private short extend;
    private List<TxDefine> validTxs;
    private List<MessageDefine> validMsgs;
    private String invalidTxs;
    private String invalidMsgs;

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

    public List<TxDefine> getValidTxs() {
        return validTxs;
    }

    public void setValidTxs(List<TxDefine> validTxs) {
        this.validTxs = validTxs;
    }

    public List<MessageDefine> getValidMsgs() {
        return validMsgs;
    }

    public void setValidMsgs(List<MessageDefine> validMsgs) {
        this.validMsgs = validMsgs;
    }

    public String getInvalidTxs() {
        return invalidTxs;
    }

    public void setInvalidTxs(String invalidTxs) {
        this.invalidTxs = invalidTxs;
    }

    public String getInvalidMsgs() {
        return invalidMsgs;
    }

    public void setInvalidMsgs(String invalidMsgs) {
        this.invalidMsgs = invalidMsgs;
    }

}
