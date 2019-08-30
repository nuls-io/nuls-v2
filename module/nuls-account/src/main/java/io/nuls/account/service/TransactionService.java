/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
 * 账户相关交易接口定义
 * account service definition
 *
 * @author: qinyifeng
 */
public interface TransactionService {


    /**
     * 转账交易验证器(协议升级扫描)
     *
     * @param chain
     * @param tx
     * @return
     * @throws NulsException
     */
    Result transferTxValidate(Chain chain, Transaction tx) throws NulsException;

    /**
     * 多地址转账
     *
     * @param chain    当前链ID
     * @param transferDTO Data to be assembled
     * @return transfer transaction hash
     * @throws NulsException
     */
    Transaction transfer(Chain chain, TransferDTO transferDTO) throws NulsException;


    /**
     * 创建多签交易, 交易from中只能有同一个多签地址
     * <p>
     * create multi sign transfer transaction
     *
     * @param chain                chain
     * @param multiSignTransferDTO Data to be assembled
     * @return
     */
    MultiSignTransactionResultDTO multiSignTransfer(Chain chain, MultiSignTransferDTO multiSignTransferDTO) throws NulsException;


    /**
     * 多签交易签名
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
     * 创建多签账户设置别名交易
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
     * 组装一个不包含签名的设置别名的交易(适用于普通地址)
     * @param chain
     * @param address
     * @param aliasName
     * @return
     * @throws NulsException
     */
    Transaction createSetAliasTxWithoutSign(Chain chain, Address address, String aliasName) throws NulsException;

    /**
     * 组装一个不包含签名的设置别名的交易(适用于多签地址)
     * @param chain
     * @param address
     * @param aliasName
     * @param msign 多签地址最小签名数
     * @return
     * @throws NulsException
     */
    Transaction createSetAliasTxWithoutSign(Chain chain, Address address, String aliasName, int msign) throws NulsException;

//    /**
//     * 多签交易处理
//     * 如果达到最少签名数则广播交易，否则什么也不做
//     **/
//    boolean txMutilProcessing(Chain chain, MultiSigAccount multiSigAccount, Transaction tx, TransactionSignature txSignature) throws NulsException;

}
