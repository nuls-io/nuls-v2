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

package io.nuls.protocol.storage.impl;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.protocol.constant.Constant;
import io.nuls.protocol.model.po.StatisticsInfo;
import io.nuls.protocol.storage.StatisticsStorageService;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.protocol.utils.LoggerUtil.COMMON_LOG;

/**
 * 统计信息持久化类实现
 *
 * @author captain
 * @version 1.0
 * @date 2019/4/23 11:03
 */
@Component
public class StatisticsStorageServiceImpl implements StatisticsStorageService {

    @Override
    public boolean save(int chainId, StatisticsInfo statisticsInfo) {
        byte[] bytes;
        try {
            bytes = statisticsInfo.serialize();
            return RocksDBService.put(Constant.STATISTICS + chainId, ByteUtils.longToBytes(statisticsInfo.getHeight()), bytes);
        } catch (Exception e) {
            COMMON_LOG.error(e);
            return false;
        }
    }

    @Override
    public StatisticsInfo get(int chainId, long height) {
        try {
            StatisticsInfo po = new StatisticsInfo();
            byte[] bytes = RocksDBService.get(Constant.STATISTICS+chainId, ByteUtils.longToBytes(height));
            po.parse(new NulsByteBuffer(bytes));
            return po;
        } catch (Exception e) {
            COMMON_LOG.error(e);
            return null;
        }
    }

    @Override
    public boolean delete(int chainId, long height) {
        try {
            return RocksDBService.delete(Constant.STATISTICS+chainId, ByteUtils.longToBytes(height));
        } catch (Exception e) {
            COMMON_LOG.error(e);
            return false;
        }
    }

    @Override
    public List<StatisticsInfo> getList(int chainId) {
        try {
            var pos = new ArrayList<StatisticsInfo>();
            List<byte[]> valueList = RocksDBService.valueList(Constant.STATISTICS+chainId);
            for (byte[] bytes : valueList) {
                var po = new StatisticsInfo();
                po.parse(new NulsByteBuffer(bytes));
                pos.add(po);
            }
            return pos;
        } catch (Exception e) {
            COMMON_LOG.error(e);
            return null;
        }
    }
}
