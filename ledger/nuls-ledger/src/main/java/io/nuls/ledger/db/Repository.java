package io.nuls.ledger.db;

import io.nuls.ledger.model.AccountState;

import java.math.BigInteger;

public interface Repository {

    AccountState createAccount(short chainId, byte[] addr);

    boolean isExist(byte[] addr);

    AccountState getAccountState(byte[] addr);

    void delete(byte[] addr);

    long increaseNonce(byte[] addr);

    long setNonce(byte[] addr, long nonce);

    long getNonce(byte[] addr);

    long getBalance(byte[] addr);

    long addBalance(byte[] addr, long value);
}
