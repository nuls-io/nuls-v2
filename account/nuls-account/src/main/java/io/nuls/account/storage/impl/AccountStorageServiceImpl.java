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
import io.nuls.account.constant.AccountParam;
import io.nuls.account.constant.AccountStorageConstant;
import io.nuls.account.model.po.AccountPo;
import io.nuls.account.storage.AccountStorageService;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

import java.io.File;
import java.io.IOException;
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
        //读取配置文件，数据存储根目录，初始化打开该目录下包含的表连接并放入缓存
        RocksDBService.init(AccountParam.getInstance().getDataPath());
        try {
            RocksDBService.createTable(AccountStorageConstant.DB_NAME_ACCOUNT);
        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                Log.error(e.getMessage());
                throw new NulsRuntimeException(AccountErrorCode.DB_TABLE_CREATE_ERROR);
            }
        }
    }

    @Override
    public boolean saveAccountList(List<AccountPo> accountPoList) {
        Map<byte[], byte[]> accountPoMap = new HashMap<>();
        try {
            for (AccountPo po : accountPoList) {
                //序列化对象为byte数组存储
                accountPoMap.put(po.getAddressObj().getAddressBytes(), po.serialize());
            }
            return RocksDBService.batchPut(AccountStorageConstant.DB_NAME_ACCOUNT, accountPoMap);
        } catch (Exception e) {
            Log.error(e.getMessage());
            throw new NulsRuntimeException(AccountErrorCode.DB_SAVE_BATCH_ERROR);
        }
    }

}
