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

package io.nuls.block.thread.monitor;

import com.google.common.collect.Lists;
import io.nuls.block.config.ConfigLoader;
import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ChainManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.Chain;
import io.nuls.block.utils.ChainGenerator;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static io.nuls.block.constant.Constant.CHAIN_ID;
import static io.nuls.block.constant.Constant.MODULES_CONFIG_FILE;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RocksDBService.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*"})
public class ForkChainsMonitorTest {

    @BeforeClass
    public static void set() throws Exception {
        SpringLiteContext.init("io.nuls.block", new ModularServiceMethodInterceptor());
    }

    @After
    public void tearDown() throws Exception {
        ChainManager.setMasterChain(CHAIN_ID, null);
        ChainManager.setOrphanChains(CHAIN_ID, null);
        ChainManager.setForkChains(CHAIN_ID, null);
    }

    @Test
    public void testRun() throws Exception {
        PowerMockito.mockStatic(RocksDBService.class);
        PowerMockito.when(RocksDBService.deleteKeys(anyString(), anyList())).thenReturn(true);
        Assert.assertTrue(RocksDBService.deleteKeys("aaa", Lists.newArrayList()));

        ConfigLoader.load(MODULES_CONFIG_FILE);
        ContextManager.init(CHAIN_ID);
        ContextManager.getContext(CHAIN_ID).setStatus(RunningStatusEnum.RUNNING);
        Chain masterChain = ChainGenerator.newMasterChain(999L, "A", CHAIN_ID);
//        masterChain.setEndHash(NulsDigestData.calcDigestData(("A" + (999)).getBytes()));
        ChainManager.setMasterChain(CHAIN_ID, masterChain);

        Chain chainB = ChainGenerator.newChain(100, 900, "B", masterChain, "A", CHAIN_ID);
        Chain chainC = ChainGenerator.newChain(200, 1000, "C", chainB, "B", CHAIN_ID);
        Chain chainD = ChainGenerator.newChain(300, 1100, "D", chainB, "B", CHAIN_ID);
        Chain chainE = ChainGenerator.newChain(400, 1000, "E", chainB, "B", CHAIN_ID);
        Chain chainF = ChainGenerator.newChain(500, 1000, "F", chainD, "D", CHAIN_ID);
        ChainManager.addForkChain(CHAIN_ID, chainB);
        ChainManager.addForkChain(CHAIN_ID, chainC);
        ChainManager.addForkChain(CHAIN_ID, chainD);
        ChainManager.addForkChain(CHAIN_ID, chainE);
        ChainManager.addForkChain(CHAIN_ID, chainF);

        ForkChainsMonitor.getInstance().run();

        Assert.assertEquals(1100, masterChain.getEndHeight());
    }

    @Test
    public void test1() {
        int count = 0;
        long forkHeight = 100;
        long rollbackHeight = 999;
        do{
            //模拟回滚成功
            rollbackHeight--;
            count++;
        } while (rollbackHeight >= forkHeight);
        Assert.assertEquals(900, count);
    }
}