package io.nuls.ledger.service;

import io.nuls.ledger.model.AccountState;

import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/29.
 */
public interface AccountStateService {

    AccountState createAccount(int chainId, String addr, int assetId);

    boolean isExist(String addr,int chainId, int assetId);

    AccountState getAccountState(String addr,int chainId, int assetId);

    long increaseNonce(String addr,int chainId, int assetId);

    long setNonce(String addr,int chainId, int assetId, long nonce);

    long getNonce(String addr,int chainId, int assetId);

    BigInteger getBalance(String addr,int chainId, int assetId);

    BigInteger addBalance(String addr,int chainId, int assetId, BigInteger value);

    /**
     * 根据高度冻结用户的余额
     *
     * @param addr
     * @param txHash
     * @param amount
     * @param height
     * @return
     */
    BigInteger freezeByHeight(String addr,int chainId, int assetId, String txHash, BigInteger amount, long height);

    BigInteger unfreezeByHeight(String address,int chainId, int assetId, long latestHeight);

    /**
     * 根据时间冻结用户的余额
     *
     * @param addr
     * @param txHash
     * @param amount
     * @param lockTime
     * @return
     */
    BigInteger freezeByLockTime(String addr,int chainId, int assetId, String txHash, BigInteger amount, long lockTime);

    BigInteger unfreezeLockTime(String address,int chainId, int assetId, long latestBlockTime);
}
