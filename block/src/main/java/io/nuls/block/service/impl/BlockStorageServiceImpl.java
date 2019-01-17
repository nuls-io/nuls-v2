/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
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

package io.nuls.block.service.impl;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.model.po.BlockHeaderPo;
import io.nuls.block.service.BlockStorageService;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.parse.SerializeUtils;

import java.util.List;

import static io.nuls.block.constant.Constant.*;
import static io.nuls.block.utils.LoggerUtil.Log;

/**
 * 区块存储服务实现类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午11:09
 */
@Service
public class BlockStorageServiceImpl implements BlockStorageService {

    @Override
    public boolean save(int chainId, BlockHeaderPo blockHeader) {
        byte[] height = SerializeUtils.uint64ToByteArray(blockHeader.getHeight());
        try {
            byte[] hash = blockHeader.getHash().serialize();
            RocksDBService.put(BLOCK_HEADER_INDEX + chainId, height, hash);
            RocksDBService.put(BLOCK_HEADER + chainId, hash, blockHeader.serialize());
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e);
            return false;
        }
        return true;
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
            e.printStackTrace();
            Log.error(e);
            return null;
        }
    }

    @Override
    public BlockHeaderPo query(int chainId, NulsDigestData hash) {
        try {
            byte[] bytes = RocksDBService.get(BLOCK_HEADER + chainId, hash.serialize());
            if (bytes == null) {
                return null;
            }
            BlockHeaderPo blockHeader = new BlockHeaderPo();
            blockHeader.parse(new NulsByteBuffer(bytes));
            return blockHeader;
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e);
            return null;
        }
    }

    @Override
    public List<BlockHeaderPo> query(int chainId, long startHeight, long endHeight) {
        return null;
    }

    @Override
    public boolean remove(int chainId, long height) {
        try {
            byte[] hash = RocksDBService.get(BLOCK_HEADER_INDEX + chainId, SerializeUtils.uint64ToByteArray(height));
            RocksDBService.delete(BLOCK_HEADER_INDEX + chainId, SerializeUtils.uint64ToByteArray(height));
            RocksDBService.delete(BLOCK_HEADER + chainId, hash);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e);
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
            e.printStackTrace();
            Log.error(e);
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
            e.printStackTrace();
            Log.error(e);
            return false;
        }
    }

}
