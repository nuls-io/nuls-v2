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

package io.nuls.block.storage;

import io.nuls.base.data.Block;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.po.BlockHeaderPo;
import io.nuls.block.service.BlockStorageService;
import io.nuls.block.test.BlockGenerator;
import io.nuls.block.utils.BlockUtil;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.nuls.block.constant.Constant.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class BlockStorageServiceImplTest {

    private static BlockHeaderPo header;
    private static final int CHAIN_ID = 1;
    private static BlockStorageService service;

    @BeforeClass
    public static void beforeClass() throws Exception {
        SpringLiteContext.init("io.nuls.block", new ModularServiceMethodInterceptor());
        service = SpringLiteContext.getBean(BlockStorageService.class);

        Block block = BlockGenerator.generate(null);
        header = BlockUtil.toBlockHeaderPo(block);
    }

    @Test
    public void save() throws Exception {
        service.save(CHAIN_ID, header);
        assertNotNull(service.query(CHAIN_ID, header.getHeight()));
    }

    @Test
    public void remove() throws Exception {
        service.save(CHAIN_ID, header);
        service.remove(CHAIN_ID, header.getHeight());
        assertNull(service.query(CHAIN_ID, header.getHeight()));
    }

}