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
import io.nuls.account.model.po.MultiSigAccountPo;
import io.nuls.account.storage.MultiSigAccountStorageService;
import io.nuls.tools.log.Log;
import io.nuls.base.data.Address;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: EdwardChan
 *
 * Dev. 19th 2018
 *
 */
@Service
public class MultiSigAccountStorageServiceImpl implements MultiSigAccountStorageService, InitializingBean {

    @Override
    public void afterPropertiesSet() {
        //If tables do not exist, create tables.
        if (!RocksDBService.existTable(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT)) {
            try {
                RocksDBService.createTable(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT);
            } catch (Exception e) {
                if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                    Log.error(e.getMessage());
                    throw new NulsRuntimeException(AccountErrorCode.DB_TABLE_CREATE_ERROR);
                }
            }
        }
    }

    @Override
    public boolean saveAccount(MultiSigAccountPo multiSigAccountPo) {
        try {
            return RocksDBService.put(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT, multiSigAccountPo.getAddress().getAddressBytes(), multiSigAccountPo.serialize());
        } catch (Exception e) {
            Log.error("",e);
            throw new NulsRuntimeException(AccountErrorCode.DB_SAVE_BATCH_ERROR);
        }
    }

    @Override
    public boolean removeAccount(Address address) {
        if (null == address || address.getAddressBytes() == null || address.getAddressBytes().length <= 0) {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        try {
            return RocksDBService.delete(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT, address.getAddressBytes());
        } catch (Exception e) {
            Log.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_SAVE_ERROR);
        }
    }

    @Override
    public List<MultiSigAccountPo> getAccountList() {
        List<MultiSigAccountPo> multiSigAccountPoList = new ArrayList<>();
        try {
            List<byte[]> list = RocksDBService.valueList(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT);
            if (list != null) {
                for (byte[] value : list) {
                    MultiSigAccountPo multiSigAccountPo = new MultiSigAccountPo();
                    //将byte数组反序列化为AccountPo返回
                    multiSigAccountPo.parse(value, 0);
                    multiSigAccountPoList.add(multiSigAccountPo);
                }
            }
        } catch (Exception e) {
            Log.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_QUERY_ERROR);
        }
        return multiSigAccountPoList;
    }

    @Override
    public MultiSigAccountPo getAccount(byte[] address) {
        byte[] multiSigAccountPoBytes = RocksDBService.get(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT, address);
        if (null == multiSigAccountPoBytes) {
            return null;
        }
        MultiSigAccountPo multiSigAccountPo = new MultiSigAccountPo();
        try {
            //将byte数组反序列化为AccountPo返回
            multiSigAccountPo.parse(multiSigAccountPoBytes, 0);
        } catch (Exception e) {
            Log.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_QUERY_ERROR);
        }
        return multiSigAccountPo;
    }
}
