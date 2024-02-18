/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.account.service;

import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.dto.MultiSignTransactionResultDTO;
import io.nuls.account.model.dto.MultiSignTransferDTO;
import io.nuls.account.model.dto.TransferDTO;
import io.nuls.base.data.Address;
import io.nuls.base.data.Transaction;
import io.nuls.core.basic.Result;
import io.nuls.core.exception.NulsException;

/**
 * Account related transaction interface definition
 * account service definition
 *
 * @author: qinyifeng
 */
public interface TransactionService {


    /**
     * Transfer transaction validator(Protocol upgrade scan)
     *
     * @param chain
     * @param tx
     * @return
     * @throws NulsException
     */
    Result transferTxValidate(Chain chain, Transaction tx) throws NulsException;

    /**
     * Multiple address transfer
     *
     * @param chain    Current ChainID
     * @param transferDTO Data to be assembled
     * @return transfer transaction hash
     * @throws NulsException
     */
    Transaction transfer(Chain chain, TransferDTO transferDTO) throws NulsException;


    /**
     * Create multi signature transactions, transactionfromOnly the same multi signature address can exist in the
     * <p>
     * create multi sign transfer transaction
     *
     * @param chain                chain
     * @param multiSignTransferDTO Data to be assembled
     * @return
     */
    MultiSignTransactionResultDTO multiSignTransfer(Chain chain, MultiSignTransferDTO multiSignTransferDTO) throws NulsException;


    /**
     * Multiple transaction signatures
     * <p>
     * sign multi sign transaction
     *
     * @param chain    chainId
     * @param account  the account which will sign the transaction
     * @param password the account's password
     * @param txStr    the hex data of transaction
     * @return MultiSignTransactionResultDto it contains two element:is broadcast and the transactio
     * @auther EdwardChan
     */
    MultiSignTransactionResultDTO signMultiSignTransaction(Chain chain, Account account, String password, String txStr)
            throws NulsException;

    /**
     * Create multi signature accounts and set alias transactions
     * @param chain
     * @param address
     * @param aliasName
     * @param signAddr
     * @param password
     * @return
     * @throws NulsException
     */
    MultiSignTransactionResultDTO setMultiSignAccountAlias(Chain chain, String address, String aliasName, String signAddr, String password) throws NulsException;

    /**
     * Assemble a transaction with an alias setting that does not include a signature(Applicable to regular addresses)
     * @param chain
     * @param address
     * @param aliasName
     * @return
     * @throws NulsException
     */
    Transaction createSetAliasTxWithoutSign(Chain chain, Address address, String aliasName) throws NulsException;

    /**
     * Assemble a transaction with an alias setting that does not include a signature(Suitable for multiple signed addresses)
     * @param chain
     * @param address
     * @param aliasName
     * @param msign Minimum number of signatures for multiple signed addresses
     * @return
     * @throws NulsException
     */
    Transaction createSetAliasTxWithoutSign(Chain chain, Address address, String aliasName, int msign) throws NulsException;

//    /**
//     * Multi signature transaction processing
//     * If the minimum number of signatures is reached, broadcast the transaction; otherwise, do nothing
//     **/
//    boolean txMutilProcessing(Chain chain, MultiSigAccount multiSigAccount, Transaction tx, TransactionSignature txSignature) throws NulsException;

}
