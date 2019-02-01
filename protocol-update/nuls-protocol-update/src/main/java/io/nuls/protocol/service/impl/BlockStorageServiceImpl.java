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

package io.nuls.protocol.service.impl;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockHeader;
import io.nuls.db.service.RocksDBService;
import io.nuls.protocol.service.BlockStorageService;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 区块存储服务实现类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午11:09
 */
@Service
public class BlockStorageServiceImpl implements BlockStorageService {

    private String BLOCK_HEADER_INDEX = "BlockHeaderIndex";
    private String BLOCK_HEADER = "BlockHeader";
    Comparator<BlockHeader> BLOCK_HEADER_COMPARATOR = Comparator.comparingLong(BlockHeader::getHeight);

    @Override
    public boolean save(int chainId, BlockHeader blockHeader) {
        byte[] height = SerializeUtils.uint64ToByteArray(blockHeader.getHeight());
        try {
            byte[] hash = blockHeader.getHash().serialize();
            boolean b1 = RocksDBService.put(BLOCK_HEADER_INDEX + chainId, height, hash);
            boolean b2 = RocksDBService.put(BLOCK_HEADER + chainId, hash, blockHeader.serialize());
            return b1 && b2;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
            return null;
        }
        List<BlockHeader> blockHeaders = new ArrayList<>();
        for (byte[] bytes : valueList) {
            BlockHeader header = new BlockHeader();
            try {
                header.parse(new NulsByteBuffer(bytes));
            } catch (NulsException e) {
                e.printStackTrace();
                return null;
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
            e.printStackTrace();
            return false;
        }
    }

}
