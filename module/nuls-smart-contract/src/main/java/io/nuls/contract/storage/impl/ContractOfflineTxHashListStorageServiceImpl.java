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
import io.nuls.contract.model.po.ContractOfflineTxHashPo;
import io.nuls.contract.storage.ContractOfflineTxHashListStorageService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rockdb.service.RocksDBService;

import static io.nuls.contract.constant.ContractDBConstant.DB_NAME_CONTRACT_OFFLINE_TX_HASH_LIST;

/**
 * @author: PierreLuo
 * @date: 2019-05-24
 */
@Component
public class ContractOfflineTxHashListStorageServiceImpl implements ContractOfflineTxHashListStorageService {

    private final String baseArea = DB_NAME_CONTRACT_OFFLINE_TX_HASH_LIST + "_";


    @Override
    public Result saveOfflineTxHashList(int chainId, byte[] blockHash, ContractOfflineTxHashPo po) throws Exception {
        if (blockHash == null || po == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        boolean result = RocksDBService.put(baseArea + chainId, blockHash, po.serialize());
        if (result) {
            return ContractUtil.getSuccess();
        } else {
            return ContractUtil.getFailed();
        }
    }

    @Override
    public Result deleteOfflineTxHashList(int chainId, byte[] blockHash) throws Exception {
        if (blockHash == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        boolean result = RocksDBService.delete(baseArea + chainId, blockHash);
        if (result) {
            return ContractUtil.getSuccess();
        } else {
            return ContractUtil.getFailed();
        }
    }

    @Override
    public Result<ContractOfflineTxHashPo> getOfflineTxHashList(int chainId, byte[] blockHash) throws NulsException {
        if (blockHash == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        byte[] poBytes = RocksDBService.get(baseArea + chainId, blockHash);
        ContractOfflineTxHashPo po = new ContractOfflineTxHashPo();
        po.parse(poBytes, 0);
        return ContractUtil.getSuccess().setData(po);
    }
}
