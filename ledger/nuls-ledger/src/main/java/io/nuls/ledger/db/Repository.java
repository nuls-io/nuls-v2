package io.nuls.ledger.db;

import io.nuls.ledger.model.AccountState;

import java.math.BigInteger;

public interface Repository {

    AccountState createAccount(short chainId, byte[] addr);

    boolean isExist(byte[] addr);

    AccountState getAccountState(byte[] addr);

    void delete(byte[] addr);

    BigInteger increaseNonce(byte[] addr);

    BigInteger setNonce(byte[] addr, BigInteger nonce);

    BigInteger getNonce(byte[] addr);

    BigInteger getBalance(byte[] addr);

    BigInteger addBalance(byte[] addr, BigInteger value);
}
