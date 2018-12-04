package io.nuls.ledger.service;

import io.nuls.ledger.model.AccountState;

import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/29.
 */
public interface AccountStateService {

    AccountState createAccount(String address, int chainId, int assetId);

    boolean isExist(String address, int chainId, int assetId);

    AccountState getAccountState(String address, int chainId, int assetId);

    long increaseNonce(String address, int chainId, int assetId);

    long setNonce(String address, int chainId, int assetId, long nonce);

    long getNonce(String address, int chainId, int assetId);

    BigInteger getBalance(String address, int chainId, int assetId);

    BigInteger addBalance(String address, int chainId, int assetId, BigInteger value);

    /**
     * 从from转账到to
     *
     * @param fromAddress
     * @param toAddress
     * @param chainId
     * @param assetId
     * @param value
     */
    void transfer(String fromAddress,
                  String toAddress,
                  int chainId,
                  int assetId,
                  BigInteger value);

    /**
     * 根据高度冻结用户的余额
     *
     * @param address
     * @param txHash
     * @param amount
     * @param height
     * @return
     */
    BigInteger freezeByHeight(String address, int chainId, int assetId, String txHash, BigInteger amount, long height);

    BigInteger unfreezeByHeight(String addressess, int chainId, int assetId, long latestHeight);

    /**
     * 根据时间冻结用户的余额
     *
     * @param address
     * @param txHash
     * @param amount
     * @param lockTime
     * @return
     */
    BigInteger freezeByLockTime(String address, int chainId, int assetId, String txHash, BigInteger amount, long lockTime);

    BigInteger unfreezeLockTime(String addressess, int chainId, int assetId, long latestBlockTime);
}
