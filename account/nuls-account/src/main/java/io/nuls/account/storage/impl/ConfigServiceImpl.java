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

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountStorageConstant;
import io.nuls.account.model.bo.config.ConfigBean;
import io.nuls.account.storage.ConfigService;
import io.nuls.account.util.log.LogUtil;
import io.nuls.db.model.Entry;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.data.ObjectUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置信息存储管理类
 * Configuration Information Storage Management Class
 *
 * @author qinyifeng
 * @date 2018/12/11
 */
@Service
public class ConfigServiceImpl implements ConfigService, InitializingBean {

    @Override
    public void afterPropertiesSet() throws NulsException {
        if (!RocksDBService.existTable(AccountStorageConstant.DB_NAME_ACCOUNT_CONGIF)) {
            try {
                RocksDBService.createTable(AccountStorageConstant.DB_NAME_ACCOUNT_CONGIF);
            } catch (Exception e) {
                LogUtil.error(e);
                throw new NulsException(e);
            }
        }
    }

    @Override
    public boolean save(ConfigBean bean, int chainID) throws Exception {
        if (bean == null) {
            return false;
        }
        return RocksDBService.put(AccountStorageConstant.DB_NAME_ACCOUNT_CONGIF, ByteUtils.intToBytes(chainID), ObjectUtils.objectToBytes(bean));
    }

    @Override
    public ConfigBean get(int chainID) {
        try {
            byte[] value = RocksDBService.get(AccountStorageConstant.DB_NAME_ACCOUNT_CONGIF, ByteUtils.intToBytes(chainID));
            return ObjectUtils.bytesToObject(value);
        } catch (Exception e) {
            LogUtil.error(e);
            return null;
        }
    }

    @Override
    public boolean delete(int chainID) {
        try {
            return RocksDBService.delete(AccountStorageConstant.DB_NAME_ACCOUNT_CONGIF, ByteUtils.intToBytes(chainID));
        } catch (Exception e) {
            LogUtil.error(e);
            return false;
        }
    }

    @Override
    public Map<Integer, ConfigBean> getList() {
        try {
            List<Entry<byte[], byte[]>> list = RocksDBService.entryList(AccountStorageConstant.DB_NAME_ACCOUNT_CONGIF);
            Map<Integer, ConfigBean> configBeanMap = new HashMap<>(AccountConstant.INIT_CAPACITY);
            if (list != null) {
                for (Entry<byte[], byte[]> entry : list) {
                    int key = ByteUtils.bytesToInt(entry.getKey());
                    ConfigBean value = ObjectUtils.bytesToObject(entry.getValue());
                    configBeanMap.put(key, value);
                }
            }
            return configBeanMap;
        } catch (Exception e) {
            LogUtil.error(e);
            return null;
        }
    }
}
