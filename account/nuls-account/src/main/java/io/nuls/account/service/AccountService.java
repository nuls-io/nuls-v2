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
import io.nuls.tools.basic.Result;

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
     * @param chainId    链ID
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
     * @param chainId    链ID
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
     * @auther EdwardChan
     *
     * Nov.10th 2018
     *
     * @param chainId
     *
     * @param address
     *
     * @param password
     *
     * @return true or false
     */
     boolean setPassword(short chainId, String address, String password);

    /**
     * check if the account is encrypted
     *
     * @auther EdwardChan
     *
     * Nov.10th 2018
     *
     * @param chainId
     *
     * @param address
     *
     *
     * @return true or false
     */
    boolean isEncrypted(short chainId, String address);

    /**
     * 移除指定账户
     * Remove specified account
     * @param chainId
     * @param address
     * @param password
     * @return
     */
    public boolean removeAccount(short chainId, String address, String password);
}
