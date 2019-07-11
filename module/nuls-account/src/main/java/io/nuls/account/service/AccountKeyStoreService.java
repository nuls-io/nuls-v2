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

import io.nuls.account.model.bo.AccountKeyStore;

/**
 * 账户模块提供给外部的服务接口定义
 * account service definition
 *
 * @author: qinyifeng
 */
public interface AccountKeyStoreService {

    /**
     * 备份账户到keyStore
     * backup account to keyStore
     *
     * @param path
     * @param chainId
     * @param address  the address of the account.
     * @param password the password of the account key store.
     * @return KeyStore path
     */
    String backupAccountToKeyStore(String path, int chainId, String address, String password);

    /**
     * 获取账户到keyStore
     * backup account to keyStore
     *
     * @param chainId
     * @param address  the address of the account.
     * @param password the password of the account key store.
     * @return KeyStore path
     */
    AccountKeyStore getKeyStore(int chainId, String address, String password);

}
