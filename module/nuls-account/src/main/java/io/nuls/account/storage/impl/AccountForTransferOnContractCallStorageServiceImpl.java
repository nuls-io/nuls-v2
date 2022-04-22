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

package io.nuls.account.storage.impl;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.AccountStorageConstant;
import io.nuls.account.model.po.AccountContractCallPO;
import io.nuls.account.storage.AccountForTransferOnContractCallStorageService;
import io.nuls.account.util.LoggerUtil;
import io.nuls.base.data.Address;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rockdb.service.RocksDBService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2022/3/16
 */
@Component
public class AccountForTransferOnContractCallStorageServiceImpl implements AccountForTransferOnContractCallStorageService {


    @Override
    public boolean saveAccountList(List<AccountContractCallPO> accountPOList) {
        if (null == accountPOList || accountPOList.size() == 0) {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        Map<byte[], byte[]> accountPoMap = new HashMap<>();
        try {
            for (AccountContractCallPO po : accountPOList) {
                //序列化对象为byte数组存储
                accountPoMap.put(po.getAddress(), po.serialize());
            }
            return RocksDBService.batchPut(AccountStorageConstant.DB_NAME_ACCOUNT_CONTRACT_CALL, accountPoMap);
        } catch (Exception e) {
            LoggerUtil.LOG.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_SAVE_BATCH_ERROR);
        }
    }

    @Override
    public boolean removeAccount(List<byte[]> addresses) {
        if (null == addresses || addresses.size() <= 0) {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        try {
            return RocksDBService.deleteKeys(AccountStorageConstant.DB_NAME_ACCOUNT_CONTRACT_CALL, addresses);
        } catch (Exception e) {
            LoggerUtil.LOG.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_SAVE_ERROR);
        }
    }

    @Override
    public List<AccountContractCallPO> getAccountList() {
        List<AccountContractCallPO> accountPOList = new ArrayList<>();
        try {
            List<byte[]> list = RocksDBService.valueList(AccountStorageConstant.DB_NAME_ACCOUNT_CONTRACT_CALL);
            if (list != null) {
                for (byte[] value : list) {
                    AccountContractCallPO accountPo = new AccountContractCallPO();
                    //将byte数组反序列化为AccountPo返回
                    accountPo.parse(value, 0);
                    accountPOList.add(accountPo);
                }
            }
        } catch (Exception e) {
            LoggerUtil.LOG.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_QUERY_ERROR);
        }
        return accountPOList;
    }

    @Override
    public AccountContractCallPO getAccount(byte[] address) {
        byte[] accountBytes = RocksDBService.get(AccountStorageConstant.DB_NAME_ACCOUNT_CONTRACT_CALL, address);
        if (null == accountBytes) {
            return null;
        }
        AccountContractCallPO accountPo = new AccountContractCallPO();
        try {
            //将byte数组反序列化为AccountPo返回
            accountPo.parse(accountBytes, 0);
        } catch (Exception e) {
            LoggerUtil.LOG.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_QUERY_ERROR);
        }
        return accountPo;
    }

    @Override
    public boolean exist(byte[] address) {
        byte[] accountBytes = RocksDBService.get(AccountStorageConstant.DB_NAME_ACCOUNT_CONTRACT_CALL, address);
        if (null == accountBytes) {
            return false;
        }
        return true;
    }
}
