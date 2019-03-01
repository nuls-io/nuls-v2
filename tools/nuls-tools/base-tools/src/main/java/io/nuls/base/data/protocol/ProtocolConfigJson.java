package io.nuls.base.data.protocol;

import java.util.List;

public class ProtocolConfigJson {

    private short version;
    private short extend;
    private List<TransactionConfig> validTransactions;
    private List<MessageConfig> validMessages;
    private List<ListItem> invalidTransactions;
    private List<ListItem> invalidMessages;

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

    public List<TransactionConfig> getValidTransactions() {
        return validTransactions;
    }

    public void setValidTransactions(List<TransactionConfig> validTransactions) {
        this.validTransactions = validTransactions;
    }

    public List<MessageConfig> getValidMessages() {
        return validMessages;
    }

    public void setValidMessages(List<MessageConfig> validMessages) {
        this.validMessages = validMessages;
    }

    public List<ListItem> getInvalidTransactions() {
        return invalidTransactions;
    }

    public void setInvalidTransactions(List<ListItem> invalidTransactions) {
        this.invalidTransactions = invalidTransactions;
    }

    public List<ListItem> getInvalidMessages() {
        return invalidMessages;
    }

    public void setInvalidMessages(List<ListItem> invalidMessages) {
        this.invalidMessages = invalidMessages;
    }

    public ProtocolConfigJson() {
    }
}
