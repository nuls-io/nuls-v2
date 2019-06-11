package io.nuls.base.protocol;

import java.util.Set;

/**
 * 协议信息
 *
 * @author captain
 * @version 1.0
 * @date 2019/5/23 13:57
 */
public class Protocol {

    private short version;
    private Set<TxDefine> allowTx;
    private Set<MessageDefine> allowMsg;
    private Set<String> invalidTxs;
    private Set<String> invalidMsgs;

    public Set<String> getInvalidTxs() {
        return invalidTxs;
    }

    public void setInvalidTxs(Set<String> invalidTxs) {
        this.invalidTxs = invalidTxs;
    }

    public Set<String> getInvalidMsgs() {
        return invalidMsgs;
    }

    public void setInvalidMsgs(Set<String> invalidMsgs) {
        this.invalidMsgs = invalidMsgs;
    }

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public Set<TxDefine> getAllowTx() {
        return allowTx;
    }

    public void setAllowTx(Set<TxDefine> allowTx) {
        this.allowTx = allowTx;
    }

    public Set<MessageDefine> getAllowMsg() {
        return allowMsg;
    }

    public void setAllowMsg(Set<MessageDefine> allowMsg) {
        this.allowMsg = allowMsg;
    }

    @Override
    public String toString() {
        return "Protocol{" +
                "version=" + version +
                ", allowTx=" + allowTx +
                ", allowMsg=" + allowMsg +
                '}';
    }
}
