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
import io.nuls.protocol.model.po.ProtocolVersionPo;
import io.nuls.protocol.storage.ProtocolVersionStorageService;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.protocol.utils.LoggerUtil.commonLog;

/**
 * 统计信息持久化类实现
 *
 * @author captain
 * @version 1.0
 * @date 2019/4/23 11:03
 */
@Component
public class ProtocolVersionStorageServiceImpl implements ProtocolVersionStorageService {

    @Override
    public boolean save(int chainId, ProtocolVersionPo po) {
        byte[] bytes;
        try {
            bytes = po.serialize();
            return RocksDBService.put(Constant.PROTOCOL_VERSION_PO + chainId, ByteUtils.shortToBytes(po.getVersion()), bytes);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return false;
        }
    }

    @Override
    public ProtocolVersionPo get(int chainId, short version) {
        try {
            ProtocolVersionPo po = new ProtocolVersionPo();
            byte[] bytes = RocksDBService.get(Constant.PROTOCOL_VERSION_PO + chainId, ByteUtils.shortToBytes(version));
            po.parse(new NulsByteBuffer(bytes));
            return po;
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return null;
        }
    }

    @Override
    public boolean delete(int chainId, short version) {
        try {
            return RocksDBService.delete(Constant.PROTOCOL_VERSION_PO + chainId, ByteUtils.shortToBytes(version));
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return false;
        }
    }

    @Override
    public List<ProtocolVersionPo> getList(int chainId) {
        try {
            var pos = new ArrayList<ProtocolVersionPo>();
            List<byte[]> valueList = RocksDBService.valueList(Constant.PROTOCOL_VERSION_PO + chainId);
            for (byte[] bytes : valueList) {
                var po = new ProtocolVersionPo();
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

    @Override
    public boolean saveCurrentProtocolVersionCount(int chainId, int currentProtocolVersionCount) {
        try {
            boolean b = RocksDBService.put(Constant.CACHED_INFO + chainId, "currentProtocolVersionCount".getBytes(), ByteUtils.intToBytes(currentProtocolVersionCount));
            commonLog.debug("saveCurrentProtocolVersionCount, chainId-" + chainId + ", currentProtocolVersionCount-" + currentProtocolVersionCount + ",b-" + b);
            return b;
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return false;
        }
    }

    @Override
    public int getCurrentProtocolVersionCount(int chainId) {
        try {
            byte[] bytes = RocksDBService.get(Constant.CACHED_INFO + chainId, "currentProtocolVersionCount".getBytes());
            return ByteUtils.bytesToInt(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            return 0;
        }
    }
}
