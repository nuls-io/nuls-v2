package io.nuls.api.model.po;

public class DestroyInfo {

    private String address;

    private String reason;

    private String value;

    public DestroyInfo() {
    }

    public DestroyInfo(String address, String reason, String value) {
        this.address = address;
        this.reason = reason;
        this.value = value;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
