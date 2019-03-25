package io.nuls.api.model.po.db;

import lombok.Data;

import java.math.BigInteger;

@Data
public class AccountTokenInfo {

    private String key;

    private String address;

    private String tokenName;

    private String tokenSymbol;

    private String contractAddress;

    private BigInteger balance;

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
        this.balance = BigInteger.ZERO;
        this.decimals = decimals;
        this.isNew = true;
    }
}
