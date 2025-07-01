package io.nuls.base.api.provider.transaction.facade;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 16:40
 * @Description: Function Description
 */
public class TransactionCoinData {

    /**
     * byte[24] Account address
     */
    protected String address;

    /**
     * uint16 Asset issuance chainid
     */
    protected int assetsChainId;

    /**
     * uint16 assetid
     */
    protected int assetsId;

    /**
     * uint128 quantity
     */
    protected BigInteger amount;

    /**
     * byte[8]
     */
    private String nonce;

    private String lockTime;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAssetsChainId() {
        return assetsChainId;
    }

    public void setAssetsChainId(int assetsChainId) {
        this.assetsChainId = assetsChainId;
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

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getLockTime() {
        return lockTime;
    }

    public void setLockTime(String lockTime) {
        this.lockTime = lockTime;
    }

    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("\"address\":\"")
                .append(address).append('\"')
                .append(",\"assetsChainId\":")
                .append(assetsChainId)
                .append(",\"assetsId\":")
                .append(assetsId)
                .append(",\"amount\":")
                .append(amount)
                .append(",\"nonce\":\"")
                .append(nonce).append('\"')
                .append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionCoinData)) return false;

        TransactionCoinData that = (TransactionCoinData) o;

        if (assetsChainId != that.assetsChainId) return false;
        if (assetsId != that.assetsId) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        return nonce != null ? nonce.equals(that.nonce) : that.nonce == null;
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + assetsChainId;
        result = 31 * result + assetsId;
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (nonce != null ? nonce.hashCode() : 0);
        return result;
    }
}
