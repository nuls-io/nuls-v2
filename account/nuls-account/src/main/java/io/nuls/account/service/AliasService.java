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
 *
 */

package io.nuls.account.service;

import io.nuls.account.model.po.AliasPo;
import io.nuls.tools.basic.Result;
import io.nuls.tools.exception.NulsException;

/**
 * 账户模块内部功能服务类
 * Account module internal function service class
 *
 * @author: EdwardChan
 */
public interface AliasService {


    /**
     * 设置别名
     * set the alias of acount
     *
     * @param chainId
     * @param address   Address of account
     * @param password  password of account
     * @param aliasName the alias to set
     * @return txhash
     */
    public Result<String> setAlias(short chainId, String address, String password, String aliasName);

    /**
     * 获取设置别名交易手续费
     * Gets to set the alias transaction fee
     *
     * @param chaindId
     * @param address
     * @param aliasName
     * @return
     */
    public Result<String> getAliasFee(short chaindId, String address, String aliasName);

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
    String getAliasByAddress(short chainId, String address);

    /**
     * check whether the account is usable
     *
     * @param chainId
     * @param alias
     */
    public boolean isAliasUsable(short chainId, String alias);


    /**
     * setMutilSigAlias
     *
     * @param chainId
     * @param address
     * @param signAddress
     * @param password
     * @param alias
     * @return the hash of tx
     **/
    public String setMutilSigAlias(short chainId, String address, String signAddress, String password, String alias);

    /**
     * validate the tx of alias
     *
     * */
    public boolean aliasTxValidate(short chainId,String alias);

    /**
     * 别名交易提交
     * 1.保存别名alias至数据库
     * 2.从数据库取出对应的account账户,将别名设置进account然后保存至数据库
     * 3.将修改后的account重新进行缓存
     * aliasTxCommit
     * 1. Save the alias to the database.
     * 2. Take the corresponding account from the database, set the alias to account and save it to the database.
     * 3. Re-cache the modified account.
     */
    boolean aliasTxCommit(AliasPo aliaspo) throws NulsException;

    /**
     * 回滚别名操作(删除别名(全网))
     * 1.从数据库删除别名对象数据
     * 2.取出对应的account将别名清除,重新存入数据库
     * 3.重新缓存account
     * rollbackAlias
     * 1.Delete the alias data from the database.
     * 2. Remove the corresponding account to clear the alias and restore it in the database.
     * 3. Recache the account.
     */
    boolean rollbackAlias(AliasPo aliasPo) throws NulsException;


}
