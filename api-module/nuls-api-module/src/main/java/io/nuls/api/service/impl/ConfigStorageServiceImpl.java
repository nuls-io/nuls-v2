/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2019 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.api.service.impl;

import io.nuls.api.constant.ApiConstant;
import io.nuls.api.model.po.config.ConfigBean;
import io.nuls.api.service.ConfigStorageService;
import io.nuls.db.model.Entry;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.model.ByteUtils;
import io.nuls.tools.model.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ConfigStorageServiceImpl implements ConfigStorageService {
    @Override
    public boolean save(int chainID, ConfigBean configBean) throws Exception {
        if (configBean == null) {
            return false;
        }
        return RocksDBService.put(ApiConstant.DB_MODULE_CONFIG, ByteUtils.intToBytes(chainID), ObjectUtils.objectToBytes(configBean));
    }

    @Override
    public ConfigBean get(int chainID) {
        byte[] value = RocksDBService.get(ApiConstant.DB_MODULE_CONFIG, ByteUtils.intToBytes(chainID));
        if (value == null) {
            return null;
        }
        return ObjectUtils.bytesToObject(value);
    }

    @Override
    public boolean delete(int chainID) throws Exception {
        return RocksDBService.delete(ApiConstant.DB_MODULE_CONFIG, ByteUtils.intToBytes(chainID));
    }

    @Override
    public Map<Integer, ConfigBean> getList() {
        List<Entry<byte[], byte[]>> list = RocksDBService.entryList(ApiConstant.DB_MODULE_CONFIG);
        Map<Integer, ConfigBean> configBeanMap = new HashMap<>();
        for (Entry<byte[], byte[]> entry : list) {
            int key = ByteUtils.bytesToInt(entry.getKey());
            ConfigBean value = ObjectUtils.bytesToObject(entry.getValue());
            configBeanMap.put(key, value);
        }
        return configBeanMap;
    }
}
