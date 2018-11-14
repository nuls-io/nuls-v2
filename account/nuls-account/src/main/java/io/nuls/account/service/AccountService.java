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
import io.nuls.account.model.bo.AccountKeyStore;
import io.nuls.tools.basic.Result;
import io.nuls.tools.exception.NulsException;

import java.util.List;

/**
 * 账户模块提供给外部的服务接口定义
 * account service definition
 *
 * @author: qinyifeng
 */
public interface AccountService {

    /**
     * 创建指定个数的账户（包含地址）
     * Create a specified number of accounts,and encrypt the accounts,
     * all the accounts are encrypted by the same password
     * if the password is NULL or "", the accounts will be unencrypted.
     *
     * @param chainId  链ID
     * @param count    想要创建的账户个数
     * @param count    the number of account you want to create.
     * @param password the password of the accounts.
     * @return the account list created.
     */
    List<Account> createAccount(short chainId, int count, String password);

    /**
     * 根据账户地址字符串获取完整的账户信息
     * Query account by address.
     *
     * @param chainId 链ID
     * @param address the address of the account you want to query.
     * @return the account.
     */
    Account getAccount(short chainId, String address);

    /**
     * 获取所有账户集合,并放入缓存
     * Query all account collections and put them in cache.
     *
     * @return account list of all accounts.
     */
    List<Account> getAccountList();

    /**
     * set the password for exist account
     *
     * @param chainId
     * @param address
     * @param password
     * @return true or false
     * @auther EdwardChan
     * <p>
     * Nov.10th 2018
     */
    boolean setPassword(short chainId, String address, String password);

    /**
     * check if the account is encrypted
     *
     * @param chainId
     * @param address
     * @return true or false
     * @auther EdwardChan
     * <p>
     * Nov.10th 2018
     */
    boolean isEncrypted(short chainId, String address);

    /**
     * 移除指定账户
     * Remove specified account
     *
     * @param chainId
     * @param address
     * @param password
     * @return
     */
    public boolean removeAccount(short chainId, String address, String password);

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
     * 为账户设置备注
     * Set remark for accounts
     *
     * @param chainId
     * @param address
     * @param remark
     * @return
     */
    boolean setRemark(short chainId, String address, String remark);

    /**
     * 获取账户私钥，只返回加密账户私钥，未加密账户不返回
     * Get the account private key,Only returns the private key of the encrypted account, and the unencrypted account does not return.
     * HexUtil.encode(priKeyBytes)
     *
     * @param chainId
     * @param address
     * @param password
     * @return
     */
    String getPrivateKey(short chainId, String address, String password);

    /**
     * 获取所有本地账户账户私钥，必须保证所有账户密码一致，
     * 如果本地账户中的密码不一致，将返回错误信息
     * Get the all local private keys
     * If the password in the local account is different, the error message will be returned.
     *
     * @param chainId
     * @param password
     * @return
     */
    List<String> getAllPrivateKey(short chainId, String password);

    /**
     * 根据私钥和密码导入账户
     * import an account from plant private key and encrypt the account.
     *
     * @param chainId
     * @param prikey
     * @param password
     * @param overwrite
     * @return
     * @throws NulsException
     */
    Account importAccount(short chainId, String prikey, String password, boolean overwrite) throws NulsException;

}
