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

import io.nuls.account.model.dto.MultiSignTransactionResultDto;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.data.Transaction;

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
     * @param chainId  链ID
     * @param pubKeys    the public keys
     * @param m    the min number of sign.
     * @return the MultiSigAccount which was created.
     */
     MultiSigAccount createMultiSigAccount(int chainId, List<String> pubKeys, int m);

    /**
     * 查询多签账户
     *
     * get the multi sign account by address
     *
     * @param chainId  链ID
     * @param address    address of account
     * @return the MultiSigAccount which was created.
     */
    MultiSigAccount getMultiSigAccountByAddress(int chainId, String address);

    /**
     * 导入多签账户
     *
     * import a multi sign account
     *
     * @param chainId  链ID
     * @param address  多签账户地址
     * @param pubKeys    the public keys
     * @param m    the min number of sign.
     * @return the MultiSigAccount which was imported.
     */
    MultiSigAccount importMultiSigAccount(int chainId,String address,List<String> pubKeys, int m);

    /**
     * 移除多签账户
     *
     * remove the multiSigAccount
     *
     * @param address  地址
     * @return the result of remove the multiSigAccount
     */
    boolean removeMultiSigAccount(int chainId,String address);


    MultiSignTransactionResultDto setMultiAlias(int chainId, String address, String password, String aliasName, String signAddr);
}
