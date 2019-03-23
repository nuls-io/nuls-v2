package io.nuls.api.model.po.db;

import lombok.Data;

@Data
public class AccountTokenInfo {

    private String key;

    private String address;

    private String tokenName;

    private String tokenSymbol;

    private String contractAddress;

    private String balance;

    private int decimals;

    private boolean isNew;

    public AccountTokenInfo() {

    }

    public AccountTokenInfo(String address, String contractAddress, String tokenName, String tokenSymbol, int decimals) {
        this.key = address + contractAddress;
        this.address = address;
        this.tokenName = tokenName;
        this.tokenSymbol = tokenSymbol;
        this.contractAddress = contractAddress;
        this.balance = "0";
        this.decimals = decimals;
        this.isNew = true;
    }
}
