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
import io.nuls.tools.exception.NulsException;

import java.util.List;

/**
 * 账户模块提供给外部的服务接口定义
 * account service definition
 *
 * @author: qinyifeng
 */
public interface AccountKeyStoreService {

    /**
     * 从keyStore导入账户(密码用来验证keystore)
     * 1.从keyStore获取明文私钥(如果没有明文私钥,则通过密码从keyStore中的encryptedPrivateKey解出来)
     * 2.通过keyStore创建新账户,加密账户
     * 3.从数据库搜索此账户的别名,没有搜到则不设置(别名不从keyStore中获取,因为可能被更改)
     * 4.保存账户
     * import an account form account key store.
     *
     * @param keyStore  the keyStore of the account.
     * @param chainId
     * @param password  the password of account
     * @param overwrite
     * @return the result of the operation.
     * @throws NulsException
     */
    Account importAccountFormKeyStore(AccountKeyStore keyStore, short chainId, String password, boolean overwrite) throws NulsException;

    /**
     * 导出账户到keyStore
     * export an account to an account key store.
     *
     * @param address  the address of the account.
     * @param password the password of the account key store.
     * @return the account key store object.
     */
    AccountKeyStore exportAccountToKeyStore(short chainId, String address, String password);

}
