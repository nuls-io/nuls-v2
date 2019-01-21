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

package io.nuls.block.service.impl;

import io.nuls.base.data.Block;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.service.BlockStorageService;
import io.nuls.block.service.ChainStorageService;
import io.nuls.block.test.BlockGenerator;
import io.nuls.block.utils.BlockUtil;
import io.nuls.db.manager.RocksDBManager;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.ioc.SpringLiteContext;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.block.constant.Constant.DATA_PATH;
import static org.junit.Assert.*;

public class ChainStorageServiceImplTest {

    private static ChainStorageService service;
    private static Block block;

    @BeforeClass
    public static void beforeClass() throws Exception {
        SpringLiteContext.init("io.nuls.block");
        RocksDBService.init(DATA_PATH);
        service = SpringLiteContext.getBean(ChainStorageService.class);
        block = BlockGenerator.generate(null);
    }

    @Test
    public void singleSave() {
        NulsDigestData hash = block.getHeader().getHash();
        service.save(1, block);
        Block block1 = service.query(1, hash);
        NulsDigestData hash1 = block1.getHeader().getHash();
        System.out.println(hash);
        assertEquals(hash, hash1);
    }

    @Test
    public void singleRemove() {
        NulsDigestData hash = block.getHeader().getHash();
        service.save(1, block);
        service.remove(1, hash);
        Block block1 = service.query(1, hash);
        assertEquals(null, block1);
    }

    @Test
    public void batchSave() throws Exception {
        List<Block> list = new ArrayList<>();
        Block block2 = BlockGenerator.generate(block);
        Block block3 = BlockGenerator.generate(block2);
        Block block4 = BlockGenerator.generate(block3);
        NulsDigestData hash1 = block.getHeader().getHash();
        NulsDigestData hash2 = block2.getHeader().getHash();
        NulsDigestData hash3 = block3.getHeader().getHash();
        NulsDigestData hash4 = block4.getHeader().getHash();
        List<NulsDigestData> hashList = new ArrayList<>();
        hashList.add(hash1);
        hashList.add(hash2);
        hashList.add(hash3);
        hashList.add(hash4);
        list.add(block);
        list.add(block2);
        list.add(block3);
        list.add(block4);
        service.save(1, list);
        List<Block> blocks = service.query(1, hashList);
        NulsDigestData hash1_ = blocks.get(0).getHeader().getHash();
        NulsDigestData hash2_ = blocks.get(1).getHeader().getHash();
        NulsDigestData hash3_ = blocks.get(2).getHeader().getHash();
        NulsDigestData hash4_ = blocks.get(3).getHeader().getHash();
        assertEquals(hash1, hash1_);
        assertEquals(hash2, hash2_);
        assertEquals(hash3, hash3_);
        assertEquals(hash4, hash4_);
    }

    @Test
    public void batchRemove() throws Exception {
        List<Block> list = new ArrayList<>();
        Block block2 = BlockGenerator.generate(block);
        NulsDigestData hash1 = block.getHeader().getHash();
        NulsDigestData hash2 = block2.getHeader().getHash();
        List<NulsDigestData> hashList = new ArrayList<>();
        hashList.add(hash1);
        hashList.add(hash2);
        list.add(block);
        list.add(block2);
        service.save(1, list);
        service.remove(1, hashList);
        List<Block> blocks = service.query(1, hashList);
        assertEquals(null, blocks);
    }
}