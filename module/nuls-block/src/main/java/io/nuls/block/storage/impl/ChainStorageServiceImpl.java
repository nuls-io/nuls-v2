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
import io.nuls.base.data.Block;
import io.nuls.base.data.NulsHash;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.storage.ChainStorageService;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.nuls.block.constant.Constant.BLOCK_COMPARATOR;
import static io.nuls.block.constant.Constant.CACHED_BLOCK;
import static io.nuls.block.utils.LoggerUtil.commonLog;

/**
 * 链存储实现类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午11:09
 */
@Component
public class ChainStorageServiceImpl implements ChainStorageService {

    @Override
    public boolean save(int chainId, List<Block> blocks) {
        Map<byte[], byte[]> map = new HashMap<>(blocks.size());
        Map<String, AtomicInteger> duplicateBlockMap = ContextManager.getContext(chainId).getDuplicateBlockMap();
        try {
            for (Block block : blocks) {
                NulsHash hash = block.getHeader().getHash();
                String digestHex = hash.toHex();
                if (duplicateBlockMap.containsKey(digestHex)) {
                    duplicateBlockMap.get(digestHex).incrementAndGet();
                    continue;
                }
                byte[] key = hash.serialize();
                byte[] bytes = RocksDBService.get(CACHED_BLOCK + chainId, key);
                if (bytes != null) {
                    duplicateBlockMap.put(digestHex, new AtomicInteger(1));
                    continue;
                }
                map.put(key, block.serialize());
            }
            if (map.size() == 0) {
                return true;
            }
            boolean b = RocksDBService.batchPut(CACHED_BLOCK + chainId, map);
            commonLog.debug("ChainStorageServiceImpl-save-blocks-"+blocks.size()+"-"+b);
            return b;
        } catch (Exception e) {
            commonLog.error("", e);
        }
        return false;
    }

    @Override
    public boolean save(int chainId, Block block) {
        NulsHash hash = block.getHeader().getHash();
        String digestHex = hash.toHex();
        Map<String, AtomicInteger> duplicateBlockMap = ContextManager.getContext(chainId).getDuplicateBlockMap();
        if (duplicateBlockMap.containsKey(digestHex)) {
            duplicateBlockMap.get(digestHex).incrementAndGet();
            return true;
        }
        try {
            byte[] key = hash.serialize();
            byte[] bytes = RocksDBService.get(CACHED_BLOCK + chainId, key);
            if (bytes != null) {
                duplicateBlockMap.put(digestHex, new AtomicInteger(1));
                return true;
            }
            boolean b = RocksDBService.put(CACHED_BLOCK + chainId, key, block.serialize());
            commonLog.debug("ChainStorageServiceImpl-save-block-"+hash+"-"+b);
            return b;
        } catch (Exception e) {
            throw new NulsRuntimeException(BlockErrorCode.DB_SAVE_ERROR);
        }
    }

    @Override
    public Block query(int chainId, NulsHash hash) {
        try {
            byte[] bytes = RocksDBService.get(CACHED_BLOCK + chainId, hash.serialize());
            if (bytes == null) {
                commonLog.debug("ChainStorageServiceImpl-query-fail-hash-"+hash);
                return null;
            }
            Block block = new Block();
            block.parse(new NulsByteBuffer(bytes));
            commonLog.debug("ChainStorageServiceImpl-query-success-hash-"+hash);
            return block;
        } catch (Exception e) {
            commonLog.error("", e);
            return null;
        }
    }

    @Override
    public List<Block> query(int chainId, Deque<NulsHash> hashList) {
        List<byte[]> keys = new ArrayList<>();
        for (NulsHash hash : hashList) {
            try {
                keys.add(hash.serialize());
            } catch (IOException e) {
                return Collections.emptyList();
            }
        }
        List<byte[]> valueList = RocksDBService.multiGetValueList(CACHED_BLOCK + chainId, keys);
        if (valueList == null) {
            return Collections.emptyList();
        }
        List<Block> blockList = new ArrayList<>();
        for (byte[] bytes : valueList) {
            Block block = new Block();
            try {
                block.parse(new NulsByteBuffer(bytes));
            } catch (NulsException e) {
                commonLog.error("ChainStorageServiceImpl-batchquery-fail", e);
                return Collections.emptyList();
            }
            blockList.add(block);
        }
        blockList.sort(BLOCK_COMPARATOR);
        return blockList;
    }

    @Override
    public boolean remove(int chainId, Deque<NulsHash> hashList) {
        Map<String, AtomicInteger> duplicateBlockMap = ContextManager.getContext(chainId).getDuplicateBlockMap();
        List<byte[]> keys = new ArrayList<>();
        try {
            for (NulsHash hash : hashList) {
                String digestHex = hash.toHex();
                if (duplicateBlockMap.containsKey(digestHex)) {
                    int i = duplicateBlockMap.get(digestHex).decrementAndGet();
                    if (i == 0) {
                        duplicateBlockMap.remove(digestHex);
                    }
                    continue;
                }
                keys.add(hash.serialize());
            }
            if (keys.isEmpty()) {
                return true;
            }
            boolean b = RocksDBService.deleteKeys(CACHED_BLOCK + chainId, keys);
            commonLog.debug("ChainStorageServiceImpl-remove-hashList-"+hashList+"-"+b);
            return b;
        } catch (Exception e) {
            commonLog.error("", e);
            throw new NulsRuntimeException(BlockErrorCode.DB_DELETE_ERROR);
        }
    }

    @Override
    public boolean remove(int chainId, NulsHash hash) {
        Map<String, AtomicInteger> duplicateBlockMap = ContextManager.getContext(chainId).getDuplicateBlockMap();
        String digestHex = hash.toHex();
        if (duplicateBlockMap.containsKey(digestHex)) {
            int i = duplicateBlockMap.get(digestHex).decrementAndGet();
            if (i == 0) {
                duplicateBlockMap.remove(digestHex);
            }
            return true;
        }
        try {
            boolean b = RocksDBService.delete(CACHED_BLOCK + chainId, hash.serialize());
            commonLog.debug("ChainStorageServiceImpl-remove-hash-"+hash+"-"+b);
            return b;
        } catch (Exception e) {
            commonLog.error("", e);
            throw new NulsRuntimeException(BlockErrorCode.DB_DELETE_ERROR);
        }
    }


    @Override
    public boolean destroy(int chainId) {
        try {
            return RocksDBService.destroyTable(CACHED_BLOCK + chainId);
        } catch (Exception e) {
            commonLog.error("", e);
            throw new NulsRuntimeException(BlockErrorCode.DB_DELETE_ERROR);
        }
    }

}
