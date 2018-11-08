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

package io.nuls.account.storage;

import io.nuls.account.model.po.AccountPo;
import io.nuls.base.data.Address;
import io.nuls.tools.basic.Result;
import io.nuls.tools.exception.NulsException;

import java.util.List;

/**
 * 账户数据存储服务接口
 * Account data storage service interface
 *
 * @author: qinyifeng
 */
public interface AccountStorageService {

    /**
     * 创建账户多个账户
     * Create accounts
     * @param accountPoList 待创建的账户集合
     * @param accountPoList Account collection to be created
     * @return the result of the opration
     */
    boolean saveAccountList(List<AccountPo> accountPoList);

    /**
     * 创建账户
     * Create account
     * @param account
     * @return
     */
    boolean saveAccount(AccountPo account);

    /**
     * 删除账户
     * Delete account
     * @param address Account address to be deleted
     * @return the result of the opration
     */
    boolean removeAccount(Address address);

    /**
     * 获取所有账户
     * @return the result of the opration and Result<List<Account>>
     */
    List<AccountPo> getAccountList();

    /**
     * 根据账户获取账户信息
     * According to the account to obtain account information
     * @param address
     * @return the result of the opration
     */
    AccountPo getAccount(byte[] address);

    /**
     * 根据账户更新账户信息
     * Update account information according to the account.
     * @param account The account to be updated.
     * @return the result of the opration
     */
    boolean updateAccount(AccountPo account);

}
