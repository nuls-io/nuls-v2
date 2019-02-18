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
import io.nuls.account.model.dto.CoinDto;
import io.nuls.account.model.dto.MultiSignTransactionResultDto;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.data.Transaction;
import io.nuls.tools.basic.Result;
import io.nuls.tools.exception.NulsException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 账户相关交易接口定义
 * account service definition
 *
 * @author: qinyifeng
 */
public interface TransactionService {

    /**
     * accountTxValidate
     * 1.检查是否多个交易设置了同样的别名
     * 2.检测一个acount只能设置一个别名
     * accountTxValidate
     * 1.Check if multiple aliasTransaction have the same alias.
     * 2.Detecting an acount can only set one alias.
     *
     * @param chainId
     * @param txList 需要检查的交易列表/A list of transactions to be checked.
     *
     * @return
     */
    List<Transaction> accountTxValidate(int chainId, List<Transaction> txList) throws Exception;

    /**
     * 多地址转账
     *
     * @param currentChainId 当前链ID
     * @param fromList       从指定账户转出
     * @param toList         转出到指定账户
     * @param remark         备注
     * @return transfer transaction hash
     * @throws NulsException
     */
    String transfer(int currentChainId, List<CoinDto> fromList, List<CoinDto> toList, String remark) throws NulsException;

    /**
     * 别名转账
     *
     * the receipt address is alias
     *
     * @param chainId chainId
     * @param from       the from coin dto
     * @param to         the to coin dto
     * @param remark         remark
     * @return transfer transaction
     *
     */
    Transaction transferByAlias(int chainId, CoinDto from, CoinDto to, String remark);


    /**
     *
     * 创建多签交易
     *
     * create multi sign transfer transaction
     *
     *
     * @param chainId chainId
     * @param account       the account which will sign the transaction
     * @param password      the account's password
     * @param multiSigAccount         the multi sign account
     * @param toAddress         the to address
     * @param amount         the amount
     * @param remark         remark
     * @return MultiSignTransactionResultDto it contains two element:is broadcast and the transaction
     *
     */
    MultiSignTransactionResultDto createMultiSignTransfer(int chainId, Account account, String password, MultiSigAccount multiSigAccount, String toAddress, BigInteger amount, String remark)
            throws NulsException,IOException;

    /**
     * 多签交易签名
     *
     * sign multi sign transaction
     *
     * @auther EdwardChan
     *
     * @param chainId chainId
     * @param account       the account which will sign the transaction
     * @param password         the account's password
     * @param txHex         the hex data of transaction
     * @return MultiSignTransactionResultDto it contains two element:is broadcast and the transactio
     *
     */
    MultiSignTransactionResultDto signMultiSignTransaction(int chainId, Account account, String password, String txHex)
            throws NulsException,IOException;

    /**
     *
     * 创建多签账户设置别名交易
     *
     * create multi sign account set alias transaction
     *
     *
     * @param chainId chainId
     * @param account       the account which will sign the transaction
     * @param password      the account's password
     * @param multiSigAccount         the multi sign account
     * @param toAddress         the to address
     * @param remark         remark
     * @return MultiSignTransactionResultDto it contains two element:is broadcast and the transaction
     *
     */
    MultiSignTransactionResultDto createSetAliasMultiSignTransaction(int chainId, Account account, String password, MultiSigAccount multiSigAccount, String toAddress, String aliasName, String remark)
            throws NulsException,IOException;

    /**
     * 校验该链是否有该资产
     *
     * @param chainId
     * @param assetId
     * @return
     */
    boolean assetExist(int chainId, int assetId);

}
