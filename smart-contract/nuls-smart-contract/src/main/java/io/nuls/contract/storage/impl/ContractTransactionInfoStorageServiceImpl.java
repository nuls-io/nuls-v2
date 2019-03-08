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


import io.nuls.base.data.Address;
import io.nuls.contract.model.po.TransactionInfoPo;
import io.nuls.contract.storage.ContractTransactionInfoStorageService;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.nuls.contract.constant.ContractDBConstant.DB_NAME_CONTRACT_LEDGER_TX_INDEX;
import static io.nuls.contract.util.ContractUtil.getFailed;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @author: PierreLuo
 * @date: 2018/6/5
 */
@Component
public class ContractTransactionInfoStorageServiceImpl implements ContractTransactionInfoStorageService {


    @Override
    public Result saveTransactionInfo(int chainId, byte[] infoKey, TransactionInfoPo infoPo) throws Exception {
        boolean result = RocksDBService.put(DB_NAME_CONTRACT_LEDGER_TX_INDEX, infoKey, infoPo.serialize());
        if(result) {
            return getSuccess();
        } else {
            return getFailed();
        }
    }

    @Override
    public List<TransactionInfoPo> getTransactionInfoListByAddress(int chainId, byte[] address) throws NulsException {
        List<TransactionInfoPo> infoPoList = new ArrayList<>();
        List<byte[]> keyList = RocksDBService.keyList(DB_NAME_CONTRACT_LEDGER_TX_INDEX);
        if (keyList == null || keyList.isEmpty()) {
            return infoPoList;
        }

        byte[] addressKey = new byte[Address.ADDRESS_LENGTH];
        TransactionInfoPo transactionInfoPo;
        byte[] values;
        for (byte[] key : keyList) {
            System.arraycopy(key, 0, addressKey, 0, Address.ADDRESS_LENGTH);
            if (Arrays.equals(addressKey, address)) {
                values = RocksDBService.get(DB_NAME_CONTRACT_LEDGER_TX_INDEX, key);
                transactionInfoPo = new TransactionInfoPo();
                transactionInfoPo.parse(values, 0);
                infoPoList.add(transactionInfoPo);
            }
        }
        return infoPoList;
    }

    @Override
    public Result deleteTransactionInfo(int chainId, byte[] infoKey) throws Exception {
        boolean result = RocksDBService.delete(DB_NAME_CONTRACT_LEDGER_TX_INDEX, infoKey);
        if(result) {
            return getSuccess();
        } else {
            return getFailed();
        }
    }

    @Override
    public Result<byte[]> getTransactionInfo(int chainId, byte[] infoKey) {
        byte[] txInfoBytes = RocksDBService.get(DB_NAME_CONTRACT_LEDGER_TX_INDEX, infoKey);
        Result<byte[]> result = getSuccess();
        result.setData(txInfoBytes);
        return result;
    }
}
