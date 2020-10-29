package io.nuls.contract.vm.program;

import java.math.BigInteger;

public class ProgramAccount {

    private byte[] address;

    private BigInteger balance;

    private BigInteger freeze;

    private String nonce;

    private int assetChainId;

    private int assetId;

    public ProgramAccount(byte[] address, BigInteger balance, String nonce, int assetChainId, int assetId) {
        this.address = address;
        this.balance = balance;
        this.nonce = nonce;
        this.freeze = BigInteger.ZERO;
        this.assetChainId = assetChainId;
        this.assetId = assetId;
    }

    public byte[] getAddress() {
        return address;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public BigInteger addBalance(BigInteger value) {
        balance = balance.add(value);
        return balance;
    }

    public BigInteger getTotalBalance() {
        return balance.add(freeze);
    }

    public BigInteger addFreeze(BigInteger value) {
        freeze = freeze.add(value);
        return freeze;
    }

    public BigInteger getFreeze() {
        return freeze;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public int getAssetChainId() {
        return assetChainId;
    }

    public int getAssetId() {
        return assetId;
    }

}
