/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.storage;

import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.core.basic.Result;

import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2018/5/24
 */
public interface ContractAddressStorageService {
    /**
     * Save the contract address and create transactions for the contracthash
     *
     * @param account
     * @param hash
     * @return
     */
    Result saveContractAddress(int chainId, byte[] contractAddressBytes, ContractAddressInfoPo info);

    /**
     * Obtain contract information for creating contracts - creator、Create transactionhash、Creation time
     *
     * @param contractAddressBytes
     * @return
     */
    Result<ContractAddressInfoPo> getContractAddressInfo(int chainId, byte[] contractAddressBytes);

    /**
     * Delete contract address
     *
     * @param contractAddressBytes
     * @return
     */
    Result deleteContractAddress(int chainId, byte[] contractAddressBytes) throws Exception;

    /**
     * Check if this contract address exists based on the address
     *
     * @param contractAddressBytes
     * @return
     */
    boolean isExistContractAddress(int chainId, byte[] contractAddressBytes);

    /**
     * Obtain contract list based on creator
     *
     * @return
     */
    Result<List<ContractAddressInfoPo>> getContractInfoList(int chainId, byte[] creater);

    /**
     * Obtain all contracts across the entire network
     *
     * @return
     */
    Result<List<ContractAddressInfoPo>> getAllContractInfoList(int chainId);

}
