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

    /**
     * 根据高度冻结用户的余额
     *
     * @param addr
     * @param txHash
     * @param amount
     * @param height
     * @return
     */
    long freezeByHeight(byte[] addr, String txHash, long amount, long height);

    long unfreezeByHeight(byte[] addr);

    /**
     * 根据时间冻结用户的余额
     *
     * @param addr
     * @param txHash
     * @param amount
     * @param lockTime
     * @return
     */
    long freezeByLockTime(byte[] addr, String txHash, long amount, long lockTime);

    long unfreezeLockTime(byte[] addr);
}
