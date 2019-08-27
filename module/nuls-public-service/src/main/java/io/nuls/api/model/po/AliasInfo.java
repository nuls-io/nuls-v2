package io.nuls.api.model.po;

public class AliasInfo extends TxDataInfo {

    private String address;

    private String alias;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
