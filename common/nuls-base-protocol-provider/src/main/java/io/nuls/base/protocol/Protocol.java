package io.nuls.base.protocol;

import java.util.List;

/**
 * 协议信息
 *
 * @author captain
 * @version 1.0
 * @date 2019/5/23 13:57
 */
public class Protocol {

    private short version;
    private List<TxDefine> allowTx;
    private List<MessageDefine> allowMsg;

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public List<TxDefine> getAllowTx() {
        return allowTx;
    }

    public void setAllowTx(List<TxDefine> allowTx) {
        this.allowTx = allowTx;
    }

    public List<MessageDefine> getAllowMsg() {
        return allowMsg;
    }

    public void setAllowMsg(List<MessageDefine> allowMsg) {
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
