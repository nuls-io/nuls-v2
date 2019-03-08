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
import io.nuls.contract.model.po.ContractTokenTransferInfoPo;
import io.nuls.contract.storage.ContractTokenTransferStorageService;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.model.Entry;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsRuntimeException;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.contract.constant.ContractDBConstant.DB_NAME_CONTRACT_NRC20_TOKEN_TRANSFER;
import static io.nuls.contract.util.ContractDBUtil.getModel;
import static io.nuls.contract.util.ContractDBUtil.putModel;
import static io.nuls.contract.util.ContractUtil.getFailed;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/5
 */
@Component
public class ContractTokenTransferStorageServiceImpl implements ContractTokenTransferStorageService{

    @Override
    public Result saveTokenTransferInfo(int chainId, byte[] infoKey, ContractTokenTransferInfoPo infoPo) {
        boolean result = putModel(DB_NAME_CONTRACT_NRC20_TOKEN_TRANSFER + chainId, infoKey, infoPo);
        if(result) {
            return getSuccess();
        } else {
            return getFailed();
        }
    }

    @Override
    public List<ContractTokenTransferInfoPo> getTokenTransferInfoListByAddress(int chainId, byte[] address) {
        List<ContractTokenTransferInfoPo> infoPoList = new ArrayList<>();
        List<Entry<byte[], byte[]>> entryList = RocksDBService.entryList(DB_NAME_CONTRACT_NRC20_TOKEN_TRANSFER + chainId);
        if (entryList == null || entryList.isEmpty()) {
            return infoPoList;
        }

        ContractTokenTransferInfoPo tokenTransferInfoPo;
        for (Entry<byte[], byte[]> entry : entryList) {
            if (isAddressEquals(entry.getKey(), address)) {
                tokenTransferInfoPo = getModel(entry.getValue(), ContractTokenTransferInfoPo.class);
                infoPoList.add(tokenTransferInfoPo);
            }
        }
        return infoPoList;
    }

    private boolean isAddressEquals(byte[] key, byte[] address) {
        int length = Address.ADDRESS_LENGTH;
        for(int i = 0; i < length; i++) {
            if(key[i] != address[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<ContractTokenTransferInfoPo> getTokenTransferInfoListByAddress(int chainId, byte[] address, byte[] txHash) {
        List<ContractTokenTransferInfoPo> infoPoList = new ArrayList<>();
        List<Entry<byte[], byte[]>> entryList = RocksDBService.entryList(DB_NAME_CONTRACT_NRC20_TOKEN_TRANSFER + chainId);
        if (entryList == null || entryList.isEmpty()) {
            return infoPoList;
        }

        ContractTokenTransferInfoPo tokenTransferInfoPo;
        for (Entry<byte[], byte[]> entry : entryList) {
            if (isAddressAndHashEquals(entry.getKey(), address, txHash)) {
                tokenTransferInfoPo = getModel(entry.getValue(), ContractTokenTransferInfoPo.class);
                infoPoList.add(tokenTransferInfoPo);
            }
        }
        return infoPoList;
    }

    private boolean isAddressAndHashEquals(byte[] key, byte[] address, byte[] txHash) {
        int length = Address.ADDRESS_LENGTH + txHash.length;
        for(int i = 0, k = 0; i < length; i++) {
            if(i < Address.ADDRESS_LENGTH) {
                if(key[i] != address[i]) {
                    return false;
                }
            } else {
                if(key[i] != txHash[k++]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Result deleteTokenTransferInfo(int chainId, byte[] infoKey) throws Exception {
        boolean result = RocksDBService.delete(DB_NAME_CONTRACT_NRC20_TOKEN_TRANSFER + chainId, infoKey);
        if(result) {
            return getSuccess();
        } else {
            return getFailed();
        }
    }

    @Override
    public Result<ContractTokenTransferInfoPo> getTokenTransferInfo(int chainId, byte[] infoKey) {
        ContractTokenTransferInfoPo tokenTransferInfoPo = getModel(DB_NAME_CONTRACT_NRC20_TOKEN_TRANSFER + chainId, infoKey, ContractTokenTransferInfoPo.class);
        Result<ContractTokenTransferInfoPo> result = getSuccess();
        result.setData(tokenTransferInfoPo);
        return result;
    }
}
