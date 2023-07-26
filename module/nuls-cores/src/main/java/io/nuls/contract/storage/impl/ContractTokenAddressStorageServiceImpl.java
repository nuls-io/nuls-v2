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
package io.nuls.contract.storage.impl;


import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.storage.ContractTokenAddressStorageService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rockdb.service.RocksDBService;

import java.util.List;

import static io.nuls.contract.constant.ContractDBConstant.DB_NAME_CONTRACT_NRC20_TOKEN_ADDRESS;

/**
 * @author: PierreLuo
 * @date: 2019-03-11
 */
@Component
public class ContractTokenAddressStorageServiceImpl implements ContractTokenAddressStorageService {

    private static final byte[] EMPTY = new byte[]{0};

    private final String baseArea = DB_NAME_CONTRACT_NRC20_TOKEN_ADDRESS + "_";

    @Override
    public Result saveTokenAddress(int chainId, byte[] contractAddressBytes) throws Exception {
        if (contractAddressBytes == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        boolean result = RocksDBService.put(baseArea + chainId, contractAddressBytes, EMPTY);
        if (result) {
            return ContractUtil.getSuccess();
        } else {
            return ContractUtil.getFailed();
        }
    }


    @Override
    public Result deleteTokenAddress(int chainId, byte[] contractAddressBytes) throws Exception {
        if (contractAddressBytes == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        boolean result = RocksDBService.delete(baseArea + chainId, contractAddressBytes);
        if (result) {
            return ContractUtil.getSuccess();
        } else {
            return ContractUtil.getFailed();
        }
    }

    @Override
    public boolean isExistTokenAddress(int chainId, byte[] contractAddressBytes) {
        if (contractAddressBytes == null) {
            return false;
        }
        byte[] contract = RocksDBService.get(baseArea + chainId, contractAddressBytes);
        if (contract == null) {
            return false;
        }
        return true;
    }

    @Override
    public Result<List<byte[]>> getAllNrc20AddressList(int chainId) {
        List<byte[]> list = RocksDBService.keyList(baseArea + chainId);
        if (list == null || list.size() == 0) {
            return Result.getFailed(ContractErrorCode.DATA_NOT_FOUND);
        }
        Result<List<byte[]>> result = ContractUtil.getSuccess();
        result.setData(list);
        return result;
    }

}
