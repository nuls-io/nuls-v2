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

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.service.BlockStorageService;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class BlockStorageServiceImplTest {

    private BlockHeader header;
    private int chainId;
    private BlockStorageService service;

    @Before
    public void setUp() throws Exception {
        SpringLiteContext.init("io.nuls.block", new ModularServiceMethodInterceptor());
        chainId = 123;
        service = ContextManager.getServiceBean(BlockStorageService.class);
        header = new BlockHeader();
        header.setHeight(1212);
        header.setHash(NulsDigestData.calcDigestData("jyc".getBytes()));
        service.init(chainId);
    }

    @After
    public void tearDown() throws Exception {
        service.destroy(chainId);
        header = null;
        service = null;
    }

    @Test
    public void save() throws Exception {
        service.save(chainId, header);
        assertNotNull(service.query(chainId, header.getHeight()));
    }

    @Test
    public void remove() throws Exception {
        service.save(chainId, header);
        service.remove(chainId, header.getHeight());
        assertNull(service.query(chainId, header.getHeight()));
    }

}