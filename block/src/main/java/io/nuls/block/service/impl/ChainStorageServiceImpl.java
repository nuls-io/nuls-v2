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
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.exception.DbRuntimeException;
import io.nuls.block.service.ChainStorageService;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.nuls.block.constant.Constant.FORK_CHAINS;

/**
 * 链存储实现类
 * @author captain
 * @date 18-11-20 上午11:09
 * @version 1.0
 */
@Service
public class ChainStorageServiceImpl implements ChainStorageService {

    @Override
    public void init(int chainId) {
        try {
            if (RocksDBService.existTable(FORK_CHAINS + chainId)) {
                RocksDBService.destroyTable(FORK_CHAINS + chainId);
            }
            RocksDBService.createTable(FORK_CHAINS + chainId);
        } catch (Exception e) {
            Log.error(e);
        }
    }

    @Override
    public boolean save(int chainId, List<Block> blocks) throws Exception {
        Map<byte[], byte[]> map = new HashMap<>(blocks.size());
        for (Block block : blocks) {
            map.put(block.getHeader().getHash().getDigestBytes(), block.serialize());
        }
        RocksDBService.batchPut(FORK_CHAINS + chainId, map);
        return false;
    }

    @Override
    public boolean save(int chainId, Block block) {
        NulsDigestData hash = block.getHeader().getHash();
        byte[] key = hash.getDigestBytes();
        Log.debug("save block, hash:{}", hash);
        try {
            return RocksDBService.put(FORK_CHAINS + chainId, key, block.serialize());
        } catch (Exception e) {
            throw new DbRuntimeException("save block error!");
        }
    }

    @Override
    public Block query(int chainId, NulsDigestData hash) throws NulsException {
        byte[] bytes = RocksDBService.get(FORK_CHAINS + chainId, hash.getDigestBytes());
        Block block = new Block();
        block.parse(new NulsByteBuffer(bytes));
        return block;
    }

    @Override
    public List<Block> query(int chainId, List<NulsDigestData> hashList) throws NulsException {
        List<byte[]> keys = hashList.stream().map(e -> e.getDigestBytes()).collect(Collectors.toList());
        List<byte[]> valueList = RocksDBService.multiGetValueList(FORK_CHAINS + chainId, keys);
        List<Block> blockList = new ArrayList<>();
        for (byte[] bytes : valueList) {
            Block block = new Block();
            block.parse(new NulsByteBuffer(bytes));
            blockList.add(block);
        }
        return blockList;
    }

    @Override
    public boolean remove(int chainId, List<NulsDigestData> hashList) throws Exception {
        List<byte[]> keys = hashList.stream().map(e -> e.getDigestBytes()).collect(Collectors.toList());
        Log.debug("delete block, hash:{}", hashList.toString());
        return RocksDBService.deleteKeys(FORK_CHAINS + chainId, keys);
    }

    @Override
    public boolean remove(int chainId, NulsDigestData hash) throws Exception {
        return RocksDBService.delete(FORK_CHAINS + chainId, hash.getDigestBytes());
    }


    @Override
    public boolean destroy(int chainId) throws Exception {
        return RocksDBService.destroyTable(FORK_CHAINS + chainId);
    }

}
