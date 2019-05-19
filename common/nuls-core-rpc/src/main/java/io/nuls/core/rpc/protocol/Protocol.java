package io.nuls.core.rpc.protocol;

import java.util.List;

public class Protocol {

    private short version;
    private String moduleValidator;
    private String moduleCommit;
    private String moduleRollback;
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

    public String getModuleValidator() {
        return moduleValidator;
    }

    public void setModuleValidator(String moduleValidator) {
        this.moduleValidator = moduleValidator;
    }

    public String getModuleCommit() {
        return moduleCommit;
    }

    public void setModuleCommit(String moduleCommit) {
        this.moduleCommit = moduleCommit;
    }

    public String getModuleRollback() {
        return moduleRollback;
    }

    public void setModuleRollback(String moduleRollback) {
        this.moduleRollback = moduleRollback;
    }

    public Protocol() {
    }

    @Override
    public String toString() {
        return "Protocol{" +
                "version=" + version +
                ", moduleValidator='" + moduleValidator + '\'' +
                ", moduleCommit='" + moduleCommit + '\'' +
                ", moduleRollback='" + moduleRollback + '\'' +
                ", allowTx=" + allowTx +
                ", allowMsg=" + allowMsg +
                '}';
    }
}
