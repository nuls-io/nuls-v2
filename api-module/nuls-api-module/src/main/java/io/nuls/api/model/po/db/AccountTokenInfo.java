package io.nuls.api.model.po.db;

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
        this.setKey(address + contractAddress);
        this.address = address;
        this.tokenName = tokenName;
        this.tokenSymbol = tokenSymbol;
        this.contractAddress = contractAddress;
        this.balance = "0";
        this.decimals = decimals;
        this.isNew = true;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTokenSymbol() {
        return tokenSymbol;
    }

    public void setTokenSymbol(String tokenSymbol) {
        this.tokenSymbol = tokenSymbol;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }
}
