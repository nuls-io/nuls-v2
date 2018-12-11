/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
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

    String setNonce(String address, int chainId, int assetId, String nonce);

    String getNonce(String address, int chainId, int assetId);

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
    BigInteger freezeByHeight(String address, int chainId, int assetId, String txHash, BigInteger amount, BigInteger height);

    BigInteger unfreezeByHeight(String addressess, int chainId, int assetId, BigInteger latestHeight);

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
