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
package io.nuls.contract.storage;


import io.nuls.core.basic.Result;

import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2019-03-11
 */
public interface ContractTokenAddressStorageService {

    /**
     * 保存合约地址
     *
     * @param account
     * @param hash
     * @return
     */
    Result saveTokenAddress(int chainId, byte[] contractAddressBytes) throws Exception;


    /**
     * 删除合约地址
     *
     * @param contractAddressBytes
     * @return
     */
    Result deleteTokenAddress(int chainId, byte[] contractAddressBytes) throws Exception;

    /**
     * 根据地址检查是否存在这个合约地址
     *
     * @param contractAddressBytes
     * @return
     */
    boolean isExistTokenAddress(int chainId, byte[] contractAddressBytes);

    /**
     * 获取全网所有Nrc20合约
     *
     * @return
     */
    Result<List<byte[]>> getAllNrc20AddressList(int chainId);
}
