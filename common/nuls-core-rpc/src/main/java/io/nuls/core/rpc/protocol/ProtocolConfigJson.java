package io.nuls.core.rpc.protocol;

import java.util.List;

public class ProtocolConfigJson {

    private short version;
    private short extend;
    private String moduleValidator;
    private String moduleCommit;
    private String moduleRollback;
    private List<TxDefine> validTransactions;
    private List<MessageDefine> validMessages;
    private String invalidTransactions;
    private String invalidMessages;

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

    public List<TxDefine> getValidTransactions() {
        return validTransactions;
    }

    public void setValidTransactions(List<TxDefine> validTransactions) {
        this.validTransactions = validTransactions;
    }

    public List<MessageDefine> getValidMessages() {
        return validMessages;
    }

    public void setValidMessages(List<MessageDefine> validMessages) {
        this.validMessages = validMessages;
    }

    public String getInvalidTransactions() {
        return invalidTransactions;
    }

    public void setInvalidTransactions(String invalidTransactions) {
        this.invalidTransactions = invalidTransactions;
    }

    public String getInvalidMessages() {
        return invalidMessages;
    }

    public void setInvalidMessages(String invalidMessages) {
        this.invalidMessages = invalidMessages;
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
}
