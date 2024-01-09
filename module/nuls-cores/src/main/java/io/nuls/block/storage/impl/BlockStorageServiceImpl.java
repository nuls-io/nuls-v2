/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.block.storage.impl;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.po.BlockHeaderPo;
import io.nuls.block.storage.BlockStorageService;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rockdb.service.RocksDBService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.nuls.base.data.BlockHeader.BLOCK_HEADER_COMPARATOR;
import static io.nuls.block.constant.Constant.*;
import static io.nuls.block.utils.LoggerUtil.COMMON_LOG;

/**
 * 区块存储服务实现类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午11:09
 */
@Component
public class BlockStorageServiceImpl implements BlockStorageService {

    @Override
    public boolean save(int chainId, BlockHeaderPo blockHeader) {
        byte[] height = SerializeUtils.uint64ToByteArray(blockHeader.getHeight());
        try {
            byte[] hash = blockHeader.getHash().getBytes();
            boolean b1 = RocksDBService.put(BLOCK_HEADER_INDEX + chainId, height, hash);
            boolean b2 = RocksDBService.put(BLOCK_HEADER + chainId, hash, blockHeader.serialize());
            return b1 && b2;
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return false;
        }
    }

    @Override
    public BlockHeaderPo query(int chainId, long height) {
        try {
            byte[] key = SerializeUtils.uint64ToByteArray(height);
            byte[] hash = RocksDBService.get(BLOCK_HEADER_INDEX + chainId, key);
            if (hash == null) {
                return null;
            }
            byte[] bytes = RocksDBService.get(BLOCK_HEADER + chainId, hash);
            if (bytes == null) {
                return null;
            }
            BlockHeaderPo blockHeader = new BlockHeaderPo();
            blockHeader.parse(new NulsByteBuffer(bytes));
            return blockHeader;
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return null;
        }
    }

    @Override
    public BlockHeaderPo query(int chainId, NulsHash hash) {
        try {
            byte[] bytes = RocksDBService.get(BLOCK_HEADER + chainId, hash.getBytes());
            if (bytes == null) {
                return null;
            }
            BlockHeaderPo blockHeader = new BlockHeaderPo();
            blockHeader.parse(new NulsByteBuffer(bytes));
            return blockHeader;
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return null;
        }
    }

    @Override
    public List<BlockHeader> query(int chainId, long startHeight, long endHeight) {
        ArrayList<byte []> keys = new ArrayList<>();
        for (long i = startHeight; i <= endHeight; i++) {
            keys.add(SerializeUtils.uint64ToByteArray(i));
        }
        List<byte[]> valueList = RocksDBService.multiGetValueList(BLOCK_HEADER + chainId, keys);
        if (valueList == null) {
            return Collections.emptyList();
        }
        List<BlockHeader> blockHeaders = new ArrayList<>();
        for (byte[] bytes : valueList) {
            BlockHeader header = new BlockHeader();
            try {
                header.parse(new NulsByteBuffer(bytes));
            } catch (NulsException e) {
                COMMON_LOG.error("ChainStorageServiceImpl-batch-query-fail", e);
                return Collections.emptyList();
            }
            blockHeaders.add(header);
        }
        blockHeaders.sort(BLOCK_HEADER_COMPARATOR);
        return blockHeaders;
    }

    @Override
    public boolean remove(int chainId, long height) {
        try {
            byte[] hash = RocksDBService.get(BLOCK_HEADER_INDEX + chainId, SerializeUtils.uint64ToByteArray(height));
            boolean b1 = RocksDBService.delete(BLOCK_HEADER_INDEX + chainId, SerializeUtils.uint64ToByteArray(height));
            boolean b2 = RocksDBService.delete(BLOCK_HEADER + chainId, hash);
            return b1 && b2;
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return false;
        }
    }

    @Override
    public boolean destroy(int chainId) {
        try {
            boolean b1 = RocksDBService.destroyTable(BLOCK_HEADER + chainId);
            boolean b2 = RocksDBService.destroyTable(BLOCK_HEADER_INDEX + chainId);
            return b1 && b2;
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return false;
        }
    }

    @Override
    public long queryLatestHeight(int chainId) {
        byte[] bytes = RocksDBService.get(CHAIN_LATEST_HEIGHT, ByteUtils.intToBytes(chainId));
        return SerializeUtils.readUint64(bytes, 0);
    }

    @Override
    public boolean setLatestHeight(int chainId, long height) {
        try {
            byte[] bytes = SerializeUtils.uint64ToByteArray(height);
            return RocksDBService.put(CHAIN_LATEST_HEIGHT, ByteUtils.intToBytes(chainId), bytes);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return false;
        }
    }

}
