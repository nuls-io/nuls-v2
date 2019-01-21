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

package io.nuls.block.test;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.ioc.SpringLiteContext;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.nuls.block.constant.Constant.DATA_PATH;
import static org.junit.Assert.assertEquals;

public class TransactionStorageServiceImplTest {

    private static TransactionStorageService service;
    private static Transaction transaction;

    @BeforeClass
    public static void beforeClass() throws Exception {
        SpringLiteContext.init("io.nuls.block");
        RocksDBService.init(DATA_PATH);
        service = SpringLiteContext.getBean(TransactionStorageService.class);
        transaction = BlockGenerator.generate(null).getTxs().get(0);
    }

    @Test
    public void save() {
        NulsDigestData hash = transaction.getHash();
        service.save(1, transaction);
        Transaction transaction = service.query(1, hash);
        NulsDigestData hash1 = transaction.getHash();
        System.out.println(hash);
        assertEquals(hash, hash1);
    }

    @Test
    public void remove() {
        NulsDigestData hash = transaction.getHash();
        service.save(1, transaction);
        service.remove(1, hash);
        Transaction tx = service.query(1, hash);
        assertEquals(null, tx);
    }
}