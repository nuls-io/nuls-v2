package io.nuls.api.model.po.db;

import java.math.BigInteger;

public class CoinFromInfo {

    private String address;

    private int chainId;

    private int assetsId;

    private BigInteger amount;

    private int locked;

    private String nonce;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getAssetsId() {
        return assetsId;
    }

    public void setAssetsId(int assetsId) {
        this.assetsId = assetsId;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public int getLocked() {
        return locked;
    }

    public void setLocked(int locked) {
        this.locked = locked;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}
