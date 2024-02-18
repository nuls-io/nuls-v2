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
 *
 */

package io.nuls.account.service;

import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.base.data.Transaction;
import io.nuls.core.basic.Result;
import io.nuls.core.exception.NulsException;

/**
 * Account module internal function service class
 * Account module internal function service class
 *
 * @author: EdwardChan
 */
public interface AliasService {


    /**
     * Set alias
     * set the alias of acount
     *
     * @param chain
     * @param address   Address of account
     * @param password  password of account
     * @param aliasName the alias to set
     * @return Transaction
     */
    Transaction setAlias(Chain chain, String address, String password, String aliasName) throws Exception;

    /**
     * get the alias by address
     *
     * @param chainId
     * @param address
     * @return the alias,if the alias is not exist,it will be return null
     * @auther EdwardChan
     * <p>
     * Nov.12th 2018
     */
    String getAliasByAddress(int chainId, String address);

    /**
     * check whether the alias is usable
     *
     * @param chainId
     * @param alias
     * @return true is usable,false is unusable
     *
     */
    boolean isAliasUsable(int chainId, String alias);

    /**
     * validate the tx of alias
     *
     * @param chainId
     *
     * @param transaction
     *
     * @return the result of validate
     *
     * */
    Result aliasTxValidate(int chainId, Transaction transaction) throws NulsException;

    /**
     * Submission of alias transactions
     * 1.Save aliasaliasTo database
     * 2.Retrieve the corresponding from the databaseaccountaccount,Set alias intoaccountThen save to the database
     * 3.Modify theaccountRe cache
     * aliasTxCommit
     * 1. Save the alias to the database.
     * 2. Take the corresponding account from the database, set the alias to account and save it to the database.
     * 3. Re-cache the modified account.
     */
    boolean aliasTxCommit(int chainId, Alias alias) throws NulsException;

    /**
     * Rollback alias operation(Delete alias(Whole network))
     * 1.Delete alias object data from the database
     * 2.Take out the correspondingaccountClear alias,Re save to database
     * 3.Recacheaccount
     * rollbackAlias
     * 1.Delete the alias data from the database.
     * 2. Remove the corresponding account to clear the alias and restore it in the database.
     * 3. Recache the account.
     */
    boolean rollbackAlias(int chainId, Alias alias) throws NulsException;


}
