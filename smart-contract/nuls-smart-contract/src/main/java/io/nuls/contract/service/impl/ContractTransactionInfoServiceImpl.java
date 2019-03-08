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
package io.nuls.contract.service.impl;


import io.nuls.base.data.Address;
import io.nuls.contract.model.po.TransactionInfoPo;
import io.nuls.contract.service.ContractTransactionInfoService;
import io.nuls.contract.storage.ContractTransactionInfoStorageService;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import org.spongycastle.util.Arrays;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.nuls.contract.constant.ContractErrorCode.*;
import static io.nuls.contract.util.ContractUtil.getFailed;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/5
 */
@Service
public class ContractTransactionInfoServiceImpl implements ContractTransactionInfoService {

    @Autowired
    private ContractTransactionInfoStorageService contractTransactionInfoStorageService;

    @Override
    public Result<List<TransactionInfoPo>> getTxInfoList(int chainId, byte[] address) {
        try {
            List<TransactionInfoPo> infoPoList = contractTransactionInfoStorageService.getTransactionInfoListByAddress(chainId, address);
            return getSuccess().setData(infoPoList);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

    @Override
    public Result<Integer> saveTransactionInfo(int chainId, TransactionInfoPo infoPo, List<byte[]> addresses) {
        if (infoPo == null) {
            return Result.getFailed(NULL_PARAMETER);
        }

        if (addresses == null || addresses.size() == 0) {
            return getSuccess().setData(new Integer(0));
        }

        List<byte[]> savedKeyList = new ArrayList<>();

        try {
            byte[] txHashBytes = infoPo.getTxHash().serialize();
            int txHashLength = infoPo.getTxHash().size();
            byte[] infoKey;
            for (int i = 0; i < addresses.size(); i++) {
                infoKey = new byte[Address.ADDRESS_LENGTH + txHashLength];
                System.arraycopy(addresses.get(i), 0, infoKey, 0, Address.ADDRESS_LENGTH);
                System.arraycopy(txHashBytes, 0, infoKey, Address.ADDRESS_LENGTH, txHashLength);
                contractTransactionInfoStorageService.saveTransactionInfo(chainId, infoKey, infoPo);
                savedKeyList.add(infoKey);
            }
        } catch (Exception e) {
            try {
                for (int i = 0; i < savedKeyList.size(); i++) {
                    contractTransactionInfoStorageService.deleteTransactionInfo(chainId, savedKeyList.get(i));
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return getFailed();
        }
        return getSuccess().setData(new Integer(addresses.size()));
    }

    @Override
    public boolean isDbExistTransactionInfo(int chainId, TransactionInfoPo infoPo, byte[] address) {
        try {
            byte[] txHashBytes = infoPo.getTxHash().serialize();
            int txHashLength = infoPo.getTxHash().size();
            byte[] infoKey = new byte[Address.ADDRESS_LENGTH + txHashLength];
            System.arraycopy(address, 0, infoKey, 0, Address.ADDRESS_LENGTH);
            System.arraycopy(txHashBytes, 0, infoKey, Address.ADDRESS_LENGTH, txHashLength);
            Result<byte[]> txInfoBytesResult = contractTransactionInfoStorageService.getTransactionInfo(chainId, infoKey);
            if(txInfoBytesResult.getData() == null) {
                return false;
            } else {
                return true;
            }
        } catch (IOException e) {
            Log.error(e);
            return false;
        }
    }

    @Override
    public Result deleteTransactionInfo(int chainId, TransactionInfoPo infoPo, List<byte[]> addresses) {
        if (infoPo == null || addresses == null) {
            return Result.getFailed(NULL_PARAMETER);
        }

        int addressCount = addresses.size();

        byte[] txHashBytes;
        try {
            txHashBytes = infoPo.getTxHash().serialize();
        } catch (IOException e) {
            Log.error(e);
            return getFailed();
        }
        for (int i = 0; i < addressCount; i++) {
            try {
                contractTransactionInfoStorageService.deleteTransactionInfo(chainId, Arrays.concatenate(addresses.get(i), txHashBytes));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return getSuccess().setData(new Integer(addressCount));
    }
}
