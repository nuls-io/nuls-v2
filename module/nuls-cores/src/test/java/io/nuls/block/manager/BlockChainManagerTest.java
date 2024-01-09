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

package io.nuls.block.manager;

import io.nuls.block.constant.ChainTypeEnum;
import io.nuls.block.constant.StatusEnum;
import io.nuls.block.model.Chain;
import io.nuls.block.thread.monitor.OrphanChainsMonitor;
import io.nuls.block.utils.ChainGenerator;
import io.nuls.block.utils.ConfigLoader;
import io.nuls.core.core.ioc.SpringLiteContext;
import org.junit.*;

public class BlockChainManagerTest {

    private static final int CHAIN_ID = 2;

    @BeforeClass
    public static void set() {
        SpringLiteContext.init("io.nuls.block", "io.nuls.rpc.modulebootstrap", "io.nuls.rpc.cmd");
    }

    @Before
    public void setUp() {
        BlockChainManager.init(CHAIN_ID);
    }

    @After
    public void tearDown() {
        BlockChainManager.setMasterChain(CHAIN_ID, null);
        BlockChainManager.setOrphanChains(CHAIN_ID, null);
        BlockChainManager.setForkChains(CHAIN_ID, null);
    }

    @Test
    public void testForkOrphanChains() throws Exception {
        ConfigLoader.load();
        ContextManager.getContext(CHAIN_ID).setStatus(StatusEnum.RUNNING);
        Chain masterChain = ChainGenerator.newMasterChain(999L, "M", CHAIN_ID);
        BlockChainManager.setMasterChain(CHAIN_ID, masterChain);

        Chain chainA = ChainGenerator.newChain(100, 200, "A", null, "M", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainB = ChainGenerator.newChain(155, 170, "B", null, "C", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainC = ChainGenerator.newChain(150, 180, "C", null, "A", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainD = ChainGenerator.newChain(160, 190, "D", null, "C", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainE = ChainGenerator.newChain(170, 180, "E", null, "D", CHAIN_ID, ChainTypeEnum.ORPHAN);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainA);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainB);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainC);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainD);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainE);

        Assert.assertNotEquals(chainA, chainB);

        OrphanChainsMonitor.getInstance().run();

        Assert.assertEquals(1, chainA.getSons().size());
        Assert.assertEquals(chainC, chainA.getSons().first());
        Assert.assertEquals(0, chainB.getSons().size());
        Assert.assertEquals(chainC, chainB.getParent());
        Assert.assertEquals(chainD, chainE.getParent());
        Assert.assertEquals(chainC, chainD.getParent());
        Assert.assertEquals(0, BlockChainManager.getOrphanChains(CHAIN_ID).size());
    }

    @Test
    public void testMergeOrphanChains() throws Exception {
        ConfigLoader.load();
        ContextManager.getContext(CHAIN_ID).setStatus(StatusEnum.RUNNING);
        Chain masterChain = ChainGenerator.newMasterChain(999L, "M", CHAIN_ID);
        BlockChainManager.setMasterChain(CHAIN_ID, masterChain);

        Chain chainA = ChainGenerator.newChain(100, 199, "A", null, "M", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainB = ChainGenerator.newChain(400, 499, "B", null, "C", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainC = ChainGenerator.newChain(200, 399, "C", null, "A", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainD = ChainGenerator.newChain(450, 549, "D", null, "E", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainE = ChainGenerator.newChain(300, 449, "E", null, "C", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainF = ChainGenerator.newChain(500, 599, "F", chainD, "D", CHAIN_ID, ChainTypeEnum.ORPHAN);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainA);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainB);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainC);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainD);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainE);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainF);

        Assert.assertNotEquals(chainA, chainB);

        OrphanChainsMonitor.getInstance().run();

        Assert.assertFalse(BlockChainManager.getOrphanChains(CHAIN_ID).contains(chainB));
        Assert.assertFalse(BlockChainManager.getOrphanChains(CHAIN_ID).contains(chainC));
        Assert.assertFalse(BlockChainManager.getOrphanChains(CHAIN_ID).contains(chainD));
        Assert.assertEquals(1, chainA.getSons().size());
        Assert.assertEquals(chainE, chainA.getSons().first());
        Assert.assertEquals(chainA, chainE.getParent());
        Assert.assertEquals(499, chainA.getEndHeight());
        Assert.assertEquals(549, chainE.getEndHeight());
        Assert.assertEquals(chainE, chainF.getParent());
        Assert.assertEquals(0, BlockChainManager.getOrphanChains(CHAIN_ID).size());
    }

    /**
     * 由于不想引进mock框架,下面的代码在注释一段与数据库交互的代码后会测试通过
     *
     * @throws Exception
     */
    @Test
    public void testOrphanChains() throws Exception {
        ConfigLoader.load();
        ContextManager.getContext(CHAIN_ID).setStatus(StatusEnum.RUNNING);
        Chain masterChain = ChainGenerator.newMasterChain(999L, "A", CHAIN_ID);
//        masterChain.setEndHash(NulsHash.calcHash(("A" + (999)).getBytes()));
        BlockChainManager.setMasterChain(CHAIN_ID, masterChain);

        Chain chainB = ChainGenerator.newChain(200, 299, "B", masterChain, "A", CHAIN_ID, ChainTypeEnum.FORK);
        Chain chainC = ChainGenerator.newChain(250, 399, "C", chainB, "B", CHAIN_ID, ChainTypeEnum.FORK);
        Chain chainD = ChainGenerator.newChain(400, 499, "D", null, "C", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainE = ChainGenerator.newChain(450, 499, "E", chainD, "D", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainF = ChainGenerator.newChain(1000, 1099, "F", null, "A", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainG = ChainGenerator.newChain(300, 399, "G", null, "C", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainH = ChainGenerator.newChain(100, 199, "H", null, "A", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainI = ChainGenerator.newChain(150, 199, "I", null, "H", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainJ = ChainGenerator.newChain(200, 299, "J", null, "H", CHAIN_ID, ChainTypeEnum.ORPHAN);
        Chain chainK = ChainGenerator.newChain(250, 299, "K", chainJ, "J", CHAIN_ID, ChainTypeEnum.ORPHAN);
        BlockChainManager.addForkChain(CHAIN_ID, chainB);
        BlockChainManager.addForkChain(CHAIN_ID, chainC);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainD);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainE);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainF);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainG);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainH);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainI);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainJ);
        BlockChainManager.addOrphanChain(CHAIN_ID, chainK);

        OrphanChainsMonitor.getInstance().run();

        Assert.assertEquals(7, BlockChainManager.getForkChains(CHAIN_ID).size());
        Assert.assertEquals(0, BlockChainManager.getOrphanChains(CHAIN_ID).size());
        Assert.assertEquals(1099, masterChain.getEndHeight());
        Assert.assertEquals(499, chainC.getEndHeight());
        Assert.assertEquals(299, chainH.getEndHeight());
        Assert.assertEquals(chainC, chainE.getParent());
        Assert.assertEquals(chainH, chainK.getParent());
        Assert.assertEquals(masterChain, chainH.getParent());
        Assert.assertEquals(2, masterChain.getSons().size());

    }

}