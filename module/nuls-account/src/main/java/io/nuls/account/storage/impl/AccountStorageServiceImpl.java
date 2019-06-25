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
 *
 */

package io.nuls.account.storage.impl;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.AccountStorageConstant;
import io.nuls.account.model.po.AccountPO;
import io.nuls.account.storage.AccountStorageService;
import io.nuls.account.util.LoggerUtil;
import io.nuls.base.data.Address;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.basic.InitializingBean;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.exception.NulsRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: qinyifeng
 */
@Service
public class AccountStorageServiceImpl implements AccountStorageService, InitializingBean {

    @Override
    public void afterPropertiesSet() {
    }

    @Override
    public boolean saveAccountList(List<AccountPO> accountPOList) {
        if (null == accountPOList || accountPOList.size() == 0) {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        Map<byte[], byte[]> accountPoMap = new HashMap<>();
        try {
            for (AccountPO po : accountPOList) {
                //序列化对象为byte数组存储
                accountPoMap.put(po.getAddressObj().getAddressBytes(), po.serialize());
            }
            return RocksDBService.batchPut(AccountStorageConstant.DB_NAME_ACCOUNT, accountPoMap);
        } catch (Exception e) {
            LoggerUtil.LOG.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_SAVE_BATCH_ERROR);
        }
    }

    @Override
    public boolean saveAccount(AccountPO account) {
        try {
            return RocksDBService.put(AccountStorageConstant.DB_NAME_ACCOUNT, account.getAddressObj().getAddressBytes(), account.serialize());
        } catch (Exception e) {
            LoggerUtil.LOG.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_SAVE_BATCH_ERROR);
        }
    }

    @Override
    public boolean removeAccount(Address address) {
        if (null == address || address.getAddressBytes() == null || address.getAddressBytes().length <= 0) {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        try {
            return RocksDBService.delete(AccountStorageConstant.DB_NAME_ACCOUNT, address.getAddressBytes());
        } catch (Exception e) {
            LoggerUtil.LOG.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_SAVE_ERROR);
        }
    }

    @Override
    public List<AccountPO> getAccountList() {
        List<AccountPO> accountPOList = new ArrayList<>();
        try {
            List<byte[]> list = RocksDBService.valueList(AccountStorageConstant.DB_NAME_ACCOUNT);
            if (list != null) {
                for (byte[] value : list) {
                    AccountPO accountPo = new AccountPO();
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
    public AccountPO getAccount(byte[] address) {
        byte[] accountBytes = RocksDBService.get(AccountStorageConstant.DB_NAME_ACCOUNT, address);
        if (null == accountBytes) {
            return null;
        }
        AccountPO accountPo = new AccountPO();
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
    public boolean updateAccount(AccountPO po) {
        if (null == po) {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        if (null == po.getAddressObj()) {
            po.setAddressObj(new Address(po.getAddress()));
        }
        //校验该账户是否存在
        AccountPO account = getAccount(po.getAddressObj().getAddressBytes());
        if (null == account) {
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        try {
            //更新账户数据
            return RocksDBService.put(AccountStorageConstant.DB_NAME_ACCOUNT, po.getAddressObj().getAddressBytes(), po.serialize());
        } catch (Exception e) {
            LoggerUtil.LOG.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_UPDATE_ERROR);
        }
    }
}
