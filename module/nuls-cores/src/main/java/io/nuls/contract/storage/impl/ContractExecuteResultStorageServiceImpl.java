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
package io.nuls.contract.storage.impl;


import io.nuls.base.data.NulsHash;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.storage.ContractExecuteResultStorageService;
import io.nuls.contract.util.Log;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rockdb.service.RocksDBService;

import static io.nuls.contract.constant.ContractDBConstant.DB_NAME_CONTRACT_EXECUTE_RESULT;
import static io.nuls.contract.util.ContractDBUtil.getModel;
import static io.nuls.contract.util.ContractDBUtil.putModel;
import static io.nuls.contract.util.ContractUtil.getFailed;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/24
 */
@Component
public class ContractExecuteResultStorageServiceImpl implements ContractExecuteResultStorageService {

    private final String baseArea = DB_NAME_CONTRACT_EXECUTE_RESULT + "_";
    
    @Override
    public Result saveContractExecuteResult(int chainId, NulsHash hash, ContractResult executeResult) {
        try {
            boolean result = putModel(baseArea + chainId, hash.getBytes(), executeResult);
            if (result) {
                return getSuccess();
            } else {
                return getFailed();
            }
        } catch (Exception e) {
            Log.error("save contract execute result error", e);
            return getFailed();
        }
    }

    @Override
    public Result deleteContractExecuteResult(int chainId, NulsHash hash) {
        try {
            boolean result = RocksDBService.delete(baseArea + chainId, hash.getBytes());
            if (result) {
                return getSuccess();
            } else {
                return getFailed();
            }
        } catch (Exception e) {
            Log.error("delete contract execute result error", e);
            return getFailed();
        }
    }

    @Override
    public boolean isExistContractExecuteResult(int chainId, NulsHash hash) {
        if (hash == null) {
            return false;
        }
        byte[] contractExecuteResult = new byte[0];
        try {
            contractExecuteResult = RocksDBService.get(baseArea + chainId, hash.getBytes());
        } catch (Exception e) {
            Log.error("check contract execute result error", e);
            return false;
        }
        if (contractExecuteResult == null) {
            return false;
        }
        return true;
    }

    @Override
    public ContractResult getContractExecuteResult(int chainId, NulsHash hash) {
        if (hash == null) {
            return null;
        }
        try {
            return getModel(baseArea + chainId, hash.getBytes(), ContractResult.class);
        } catch (Exception e) {
            Log.error("get contract execute result error", e);
            return null;
        }
    }
}
