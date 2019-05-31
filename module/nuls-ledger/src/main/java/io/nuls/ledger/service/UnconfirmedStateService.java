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

import io.nuls.ledger.model.Uncfd2CfdKey;
import io.nuls.ledger.model.ValidateResult;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.AccountStateUnconfirmed;
import io.nuls.ledger.model.po.TxUnconfirmed;

import java.util.List;
import java.util.Map;

/**
 * Created by lan on 2018/12/30
 *
 * @author lanjinsheng
 */
public interface UnconfirmedStateService {

    /**
     * 重计算未确认账本信息
     *
     * @param accountState
     * @return
     */
    AccountStateUnconfirmed getUnconfirmedInfo(AccountState accountState);

    AccountStateUnconfirmed getUnconfirmedInfoAndClear(AccountState accountState);

    /**
     * 获取账本nonce信息
     *
     * @param accountState
     * @return
     */
    AccountStateUnconfirmed getUnconfirmedJustNonce(AccountState accountState);

    /**
     * 回滚信息
     *
     * @param accountState
     * @param assetKey
     * @param txsUnconfirmed
     * @param accountStateUnconfirmed
     */
    void mergeUnconfirmedNonce(AccountState accountState, String assetKey,  Map<String,TxUnconfirmed> txsUnconfirmed, AccountStateUnconfirmed accountStateUnconfirmed);

    /**
     * 回滚未确认账本交易
     *
     * @param addressChainId
     * @param assetKey
     * @param txHash
     * @return
     */
    boolean rollUnconfirmedTx(int addressChainId, String assetKey, String txHash);

    /**
     * unconfirmed tx existunconfirmed tx exist
     * @param addressChainId
     * @param assetKey
     * @param nonce
     * @return
     * @throws Exception
     */
    boolean   existTxUnconfirmedTx(int addressChainId,String assetKey,String nonce)  throws Exception;

    /**
     * delete unconfirmed state
     *
     * @param addressChainId
     * @param accountKey
     * @throws Exception
     */
    void clearAccountUnconfirmed(int addressChainId, String accountKey) throws Exception;

    void clearAllAccountUnconfirmed(int addressChainId) throws Exception;

    /**
     * batch delete unconfirmed tx
     *
     * @param addressChainId
     * @param keys
     * @throws Exception
     */
    void batchDeleteUnconfirmedTx(int addressChainId, List<Uncfd2CfdKey> keys) throws Exception;

    /**
     * update unconfirmed tx
     *
     * @param addressChainId
     * @param txNonce
     * @param txUnconfirmed
     * @param txUnconfirmed
     * @return
     */
    ValidateResult updateUnconfirmedTx(String txHash,int addressChainId, byte[] txNonce, TxUnconfirmed txUnconfirmed);
}
