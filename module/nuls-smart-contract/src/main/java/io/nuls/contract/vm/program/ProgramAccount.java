package io.nuls.contract.vm.program;

import java.math.BigInteger;

public class ProgramAccount {

    private byte[] address;

    private BigInteger balance;

    private BigInteger freeze;

    private String nonce;

    public ProgramAccount(byte[] address, BigInteger balance, String nonce) {
        this.address = address;
        this.balance = balance;
        this.nonce = nonce;
        this.freeze = BigInteger.ZERO;
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

    public void setFreeze(BigInteger freeze) {
        this.freeze = freeze;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}
