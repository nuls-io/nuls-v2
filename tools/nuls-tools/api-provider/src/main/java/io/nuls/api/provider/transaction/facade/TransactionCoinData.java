package io.nuls.api.provider.transaction.facade;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 16:40
 * @Description: 功能描述
 */
public class TransactionCoinData {

    /**
     * byte[24] 账户地址
     */
    protected String address;

    /**
     * uint16 资产发行链的id
     */
    protected int assetsChainId;

    /**
     * uint16 资产id
     */
    protected int assetsId;

    /**
     * uint128 数量
     */
    protected BigInteger amount;

    /**
     * byte[8]
     */
    private String nonce;

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
}
