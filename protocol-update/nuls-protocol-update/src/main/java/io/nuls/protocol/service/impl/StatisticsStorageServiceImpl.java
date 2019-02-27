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

package io.nuls.protocol.service.impl;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.db.service.RocksDBService;
import io.nuls.protocol.constant.Constant;
import io.nuls.protocol.model.ProtocolConfig;
import io.nuls.protocol.model.po.Statistics;
import io.nuls.protocol.service.StatisticsStorageService;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.ByteUtils;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.protocol.utils.LoggerUtil.commonLog;

@Service
public class StatisticsStorageServiceImpl implements StatisticsStorageService {

    @Override
    public boolean save(int chainId, Statistics statistics) {
        byte[] bytes;
        try {
            bytes = statistics.serialize();
            return RocksDBService.put(Constant.STATISTICS+chainId, ByteUtils.longToBytes(statistics.getHeight()), bytes);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return false;
        }
    }

    @Override
    public Statistics get(int chainId, long height) {
        try {
            Statistics po = new Statistics();
            byte[] bytes = RocksDBService.get(Constant.STATISTICS+chainId, ByteUtils.longToBytes(height));
            po.parse(new NulsByteBuffer(bytes));
            return po;
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return null;
        }
    }

    @Override
    public boolean delete(int chainId, long height) {
        try {
            return RocksDBService.delete(Constant.STATISTICS+chainId, ByteUtils.longToBytes(height));
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return false;
        }
    }

    @Override
    public List<Statistics> getList(int chainId) {
        try {
            var pos = new ArrayList<Statistics>();
            List<byte[]> valueList = RocksDBService.valueList(Constant.STATISTICS+chainId);
            for (byte[] bytes : valueList) {
                var po = new Statistics();
                po.parse(new NulsByteBuffer(bytes));
                pos.add(po);
            }
            return pos;
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return null;
        }
    }
}
