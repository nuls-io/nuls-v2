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
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.data.Transaction;
import io.nuls.core.basic.Result;
import io.nuls.core.exception.NulsException;

import java.io.IOException;

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
    MultiSignTransactionResultDTO multiSignTransfer(Chain chain, MultiSignTransferDTO multiSignTransferDTO) throws NulsException, IOException;


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
            throws NulsException, IOException;

    /**
     * 创建多签账户设置别名交易
     * <p>
     * create multi sign account set alias transaction
     *
     * @param chain           chain
     * @param account         the account which will sign the transaction
     * @param password        the account's password
     * @param multiSigAccount the multi sign account
     * @param toAddress       the to address
     * @param remark          remark
     * @return MultiSignTransactionResultDto it contains two element:is broadcast and the transaction
     */
    MultiSignTransactionResultDTO createSetAliasMultiSignTransaction(Chain chain, Account account, String password, MultiSigAccount multiSigAccount, String toAddress, String aliasName, String remark)
            throws NulsException, IOException;

    /**
     * 校验该链是否有该资产
     *
     * @param chainId
     * @param assetId
     * @return
     */
//    boolean assetExist(int chainId, int assetId);

}
