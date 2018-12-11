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
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.SerializeUtils;

import java.util.List;

import static io.nuls.block.constant.Constant.*;

/**
 * 区块存储服务实现类
 * @author captain
 * @date 18-11-20 上午11:09
 * @version 1.0
 */
@Service
public class BlockStorageServiceImpl implements BlockStorageService {

    @Override
    public void init(int chainId) {
        try {
            RocksDBService.init(DATA_PATH);
            if (!RocksDBService.existTable(CHAIN_LATEST_HEIGHT)) {
                RocksDBService.createTable(CHAIN_LATEST_HEIGHT);
            }
            if (!RocksDBService.existTable(BLOCK_HEADER + chainId)) {
                RocksDBService.createTable(BLOCK_HEADER + chainId);
            }
            if (!RocksDBService.existTable(BLOCK_HEADER_INDEX + chainId)) {
                RocksDBService.createTable(BLOCK_HEADER_INDEX + chainId);
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

    @Override
    public boolean save(int chainId, BlockHeaderPo blockHeader) {
        byte[] height = SerializeUtils.uint64ToByteArray(blockHeader.getHeight());
        byte[] hash = blockHeader.getHash().getDigestBytes();
        try {
            RocksDBService.put(BLOCK_HEADER_INDEX + chainId, height, hash);
            RocksDBService.put(BLOCK_HEADER + chainId, hash, blockHeader.serialize());
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
        return true;
    }

    @Override
    public BlockHeaderPo query(int chainId, long height) {
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
    }

    @Override
    public BlockHeaderPo query(int chainId, NulsDigestData hash) {
        byte[] bytes = RocksDBService.get(BLOCK_HEADER + chainId, hash.getDigestBytes());
        if (bytes == null) {
            return null;
        }
        BlockHeaderPo blockHeader = new BlockHeaderPo();
        blockHeader.parse(new NulsByteBuffer(bytes));
        return blockHeader;
    }

    @Override
    public List<BlockHeaderPo> query(int chainId, long startHeight, long endHeight) {
        return null;
    }

    @Override
    public boolean remove(int chainId, long height) {
        byte[] hash = RocksDBService.get(BLOCK_HEADER_INDEX + chainId, SerializeUtils.uint64ToByteArray(height));
        RocksDBService.delete(BLOCK_HEADER_INDEX + chainId, SerializeUtils.uint64ToByteArray(height));
        RocksDBService.delete(BLOCK_HEADER + chainId, hash);
        return true;
    }

    @Override
    public boolean destroy(int chainId) {
        boolean b1 = RocksDBService.destroyTable(BLOCK_HEADER + chainId);
        boolean b2 = RocksDBService.destroyTable(BLOCK_HEADER_INDEX + chainId);
        return b1 && b2;
    }

    @Override
    public long queryLatestHeight(int chainId) {
        String key = LATEST_BLOCK_HEIGHT + chainId;
        byte[] bytes = RocksDBService.get(CHAIN_LATEST_HEIGHT, key.getBytes());
        return SerializeUtils.readUint64(bytes, 0);
    }

    @Override
    public boolean setLatestHeight(int chainId, long height) {
        String key = LATEST_BLOCK_HEIGHT + chainId;
        try {
            byte[] bytes = SerializeUtils.uint64ToByteArray(height);
            return RocksDBService.put(CHAIN_LATEST_HEIGHT, key.getBytes(), bytes);
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

}
