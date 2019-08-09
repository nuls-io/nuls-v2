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
import io.nuls.account.model.po.MultiSigAccountPO;
import io.nuls.account.storage.MultiSigAccountStorageService;
import io.nuls.account.util.LoggerUtil;
import io.nuls.base.data.Address;
import io.nuls.core.basic.InitializingBean;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rockdb.service.RocksDBService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: EdwardChan
 *
 * Dev. 19th 2018
 *
 */
@Component
public class MultiSigAccountStorageServiceImpl implements MultiSigAccountStorageService, InitializingBean {

    @Override
    public void afterPropertiesSet() {
    }

    @Override
    public boolean saveAccount(MultiSigAccountPO multiSigAccountPo) {
        try {
            return RocksDBService.put(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT, multiSigAccountPo.getAddress().getAddressBytes(), multiSigAccountPo.serialize());
        } catch (Exception e) {
            LoggerUtil.LOG.error("",e);
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
            LoggerUtil.LOG.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_SAVE_ERROR);
        }
    }

    @Override
    public List<MultiSigAccountPO> getAccountList() {
        List<MultiSigAccountPO> multiSigAccountPOList = new ArrayList<>();
        try {
            List<byte[]> list = RocksDBService.valueList(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT);
            if (list != null) {
                for (byte[] value : list) {
                    MultiSigAccountPO multiSigAccountPo = new MultiSigAccountPO();
                    //将byte数组反序列化为AccountPo返回
                    multiSigAccountPo.parse(value, 0);
                    multiSigAccountPOList.add(multiSigAccountPo);
                }
            }
        } catch (Exception e) {
            LoggerUtil.LOG.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_QUERY_ERROR);
        }
        return multiSigAccountPOList;
    }

    @Override
    public MultiSigAccountPO getAccount(byte[] address) {
        byte[] multiSigAccountPoBytes = RocksDBService.get(AccountStorageConstant.DB_NAME_MULTI_SIG_ACCOUNT, address);
        if (null == multiSigAccountPoBytes) {
            return null;
        }
        MultiSigAccountPO multiSigAccountPo = new MultiSigAccountPO();
        try {
            //将byte数组反序列化为AccountPo返回
            multiSigAccountPo.parse(multiSigAccountPoBytes, 0);
        } catch (Exception e) {
            LoggerUtil.LOG.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_QUERY_ERROR);
        }
        return multiSigAccountPo;
    }
}
