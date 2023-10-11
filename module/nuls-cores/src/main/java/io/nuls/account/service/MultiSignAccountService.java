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
 */

package io.nuls.account.service;

import io.nuls.account.model.bo.Chain;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.core.exception.NulsException;

import java.util.List;

/**
 * 多签账户Service接口
 *
 * MultiSignAccount service definition
 *
 * @author: EdwardChan
 */
public interface MultiSignAccountService {

    /**
     * 创建多签账户
     *
     * Create a multi sign account
     *
     * @param chain  链
     * @param pubKeys    the public keys
     * @param m    the min number of sign.
     * @return the MultiSigAccount which was created.
     */
     MultiSigAccount createMultiSigAccount(Chain chain, List<String> pubKeys, int m) throws NulsException;

    /**
     * 查询多签账户
     *
     * get the multi sign account by address
     *
     * @param address    address String of account
     * @return the MultiSigAccount which was created.
     */
    MultiSigAccount getMultiSigAccountByAddress(String address);

    /**
     * 查询多签账户
     *
     * get the multi sign account by address
     *
     * @param address    address byte[] of account
     * @return the MultiSigAccount which was created.
     */
    MultiSigAccount getMultiSigAccountByAddress(byte[] address);

    /**
     * 移除多签账户
     *
     * remove the multiSigAccount
     *
     * @param address  地址
     * @return the result of remove the multiSigAccount
     */
    boolean removeMultiSigAccount(int chainId,String address);

}
