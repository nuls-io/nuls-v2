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

import io.nuls.ledger.model.UnconfirmedTx;
import io.nuls.ledger.model.po.AccountState;

/**
 * Created by wangkun23 on 2018/11/29.
 */
public interface AccountStateService {
    /**
     *
     * @param address
     * @param addressChainId
     * @param assetChainId
     * @param assetId
     * @return
     */
    AccountState createAccount(String address,int addressChainId, int assetChainId, int assetId);

    /**
     *
     * @param address
     * @param assetChainId
     * @param assetId
     * @return
     */
    AccountState getAccountState(String address,int addressChainId, int assetChainId, int assetId);

    /**
     *存储新的账户信息时，进行快照存储
     * @param assetKey
     * @param accountState
     */
    void updateAccountStateByTx(String assetKey,AccountState orgAccountState,AccountState accountState);

    /**
     *
     * @param assetKey
     * @param txHash
     * @param height
     */
    void rollAccountStateByTx(int addressChainId,String assetKey,String txHash,long height);

    /**
     *
     * @param addressChainId
     * @param assetKey
     * @param nonce
     * @param txHash
     */
    void rollUnconfirmTx(int addressChainId,String assetKey,String nonce,String txHash);

    /**
     *
     * @param addressChainId
     * @param newNonce
     * @param unconfirmedTx
     */
    void setUnconfirmTx(int addressChainId, String newNonce, UnconfirmedTx unconfirmedTx);



}
