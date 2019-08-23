package io.nuls.api.model.po;

import java.util.List;
import java.util.Map;

public class NulsTransfer {

    private String txHash;

    private String from;

    private String value;

    private List<Map<String, Object>> outputs;

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Map<String, Object>> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Map<String, Object>> outputs) {
        this.outputs = outputs;
    }
}
