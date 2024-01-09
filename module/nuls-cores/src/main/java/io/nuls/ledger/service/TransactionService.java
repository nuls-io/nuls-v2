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


import io.nuls.base.data.Transaction;
import io.nuls.ledger.model.ValidateResult;
import io.nuls.ledger.model.po.sub.AccountStateSnapshot;

import java.util.List;

/**
 * Transaction Business Processing Interface
 * @author lanjinsheng
 */
public interface TransactionService {


    /**
     * Unconfirmed transaction data processing
     *
     * @param addressChainId
     * @param transaction
     * @return
     * @throws Exception
     */
    ValidateResult unConfirmTxProcess(int addressChainId, Transaction transaction) throws Exception;


    /**
     * Confirmed block data submission
     *
     * @param addressChainId
     * @param txList
     * @param blockHeight
     * @return
     */
    boolean confirmBlockProcess(int addressChainId, List<Transaction> txList, long blockHeight);

    /**
     * Confirm transaction rollback processing
     *
     * @param addressChainId
     * @param preAccountStates
     * @param blockHeight
     * @return
     */
    boolean rollBackBlock(int addressChainId, List<AccountStateSnapshot> preAccountStates, long blockHeight);

    /**
     * Confirm transaction rollback processing
     *
     * @param addressChainId
     * @param blockHeight
     * @param txs
     * @return
     */
    boolean rollBackConfirmTxs(int addressChainId, long blockHeight, List<Transaction> txs);

    /**
     * Unconfirmed transaction rollback processing
     * @param addressChainId
     * @param transaction
     * @return
     */
    boolean rollBackUnconfirmTx(int addressChainId, Transaction transaction);

    /**
     *  Have you submitted it before
     * @param addressChainId
     * @param accountNonceKey
     * @return
     * @throws Exception
     */
    boolean fromNonceExist(int addressChainId, String accountNonceKey) throws Exception;

    /**
     *Does it already existhash
     * @param addressChainId
     * @param hash
     * @return
     * @throws Exception
     */
    boolean hadTxExist(int addressChainId, String hash) throws Exception;
}
