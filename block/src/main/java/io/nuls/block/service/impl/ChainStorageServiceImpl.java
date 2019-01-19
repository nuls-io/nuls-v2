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
import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.exception.DbRuntimeException;
import io.nuls.block.service.ChainStorageService;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;

import java.io.IOException;
import java.util.*;

import static io.nuls.block.constant.Constant.BLOCK_COMPARATOR;
import static io.nuls.block.constant.Constant.CACHED_BLOCK;
import static io.nuls.block.utils.LoggerUtil.Log;

/**
 * 链存储实现类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午11:09
 */
@Service
public class ChainStorageServiceImpl implements ChainStorageService {

    @Override
    public boolean save(int chainId, List<Block> blocks) {
        Map<byte[], byte[]> map = new HashMap<>(blocks.size());
        try {
            for (Block block : blocks) {
                map.put(block.getHeader().getHash().serialize(), block.serialize());
            }
            boolean b = RocksDBService.batchPut(CACHED_BLOCK + chainId, map);
            Log.debug("ChainStorageServiceImpl-save-blocks-"+blocks.size()+"-"+b);
            return b;
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e);
        }
        return false;
    }

    @Override
    public boolean save(int chainId, Block block) {
        NulsDigestData hash = block.getHeader().getHash();
        try {
            byte[] key = hash.serialize();
            boolean b = RocksDBService.put(CACHED_BLOCK + chainId, key, block.serialize());
            Log.debug("ChainStorageServiceImpl-save-hash-"+hash+"-"+b);
            return b;
        } catch (Exception e) {
            throw new DbRuntimeException("save block error!");
        }
    }

    @Override
    public Block query(int chainId, NulsDigestData hash) {
        try {
            byte[] bytes = RocksDBService.get(CACHED_BLOCK + chainId, hash.serialize());
            if (bytes == null) {
                Log.debug("ChainStorageServiceImpl-query-fail-hash-"+hash);
                return null;
            }
            Block block = new Block();
            block.parse(new NulsByteBuffer(bytes));
            Log.debug("ChainStorageServiceImpl-query-success-hash-"+hash);
            return block;
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e);
            return null;
        }
    }

    @Override
    public List<Block> query(int chainId, List<NulsDigestData> hashList) {
        List<byte[]> keys = new ArrayList<>();
        for (NulsDigestData hash : hashList) {
            try {
                keys.add(hash.serialize());
            } catch (IOException e) {
                return null;
            }
        }
        List<byte[]> valueList = RocksDBService.multiGetValueList(CACHED_BLOCK + chainId, keys);
        if (valueList == null) {
            return null;
        }
        List<Block> blockList = new ArrayList<>();
        for (byte[] bytes : valueList) {
            Block block = new Block();
            try {
                block.parse(new NulsByteBuffer(bytes));
            } catch (NulsException e) {
                Log.debug("ChainStorageServiceImpl-batchquery-fail");
                e.printStackTrace();
                Log.error(e);
                return null;
            }
            blockList.add(block);
        }
        blockList.sort(BLOCK_COMPARATOR);
        return blockList;
    }

    @Override
    public boolean remove(int chainId, List<NulsDigestData> hashList) {
        try {
            List<byte[]> keys = new ArrayList<>();
            for (NulsDigestData hash : hashList) {
                keys.add(hash.serialize());
            }
            boolean b = RocksDBService.deleteKeys(CACHED_BLOCK + chainId, keys);
            Log.debug("ChainStorageServiceImpl-remove-hashList-"+hashList+"-"+b);
            return b;
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e);
            throw new DbRuntimeException("remove blocks error!");
        }
    }

    @Override
    public boolean remove(int chainId, NulsDigestData hash) {
        try {
            boolean b = RocksDBService.delete(CACHED_BLOCK + chainId, hash.serialize());
            Log.debug("ChainStorageServiceImpl-remove-hash-"+hash+"-"+b);
            return b;
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e);
            throw new DbRuntimeException("remove block error!");
        }
    }


    @Override
    public boolean destroy(int chainId) {
        try {
            return RocksDBService.destroyTable(CACHED_BLOCK + chainId);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e);
            throw new DbRuntimeException("destroy table error!");
        }
    }

}
