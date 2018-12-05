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

package io.nuls.block;

import io.nuls.base.data.Block;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.config.GenesisBlock;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BlockGeneratorTest {

    /**
     * 测试区块生成器生成区块的连续性
     * @throws Exception
     */
    @Test
    public void generate() throws Exception {
        int start = 1;
        int count = 10;
        List<Block> blocks = new ArrayList<>();

        GenesisBlock genesisBlock = GenesisBlock.getInstance();
        blocks.add(genesisBlock);

        Block preBlock = genesisBlock;
        do{
            Block block = BlockGenerator.generate(preBlock);
            blocks.add(block);
            preBlock = block;
            start++;
        } while (start < count);

        for (int i = 0; i < blocks.size()-1; i++) {
            NulsDigestData prehash = blocks.get(i).getHeader().getHash();
            NulsDigestData hash = blocks.get(i+1).getHeader().getPreHash();
            Assert.assertEquals(prehash, hash);
        }
    }

    /**
     * 测试区块生成器生成区块的分叉
     * @throws Exception
     */
    @Test
    public void fork() throws Exception {
        Block root = BlockGenerator.generate(null);
        Block block1 = BlockGenerator.generate(root, 1);
        Block block2 = BlockGenerator.generate(root, 2);
        Assert.assertEquals(root.getHeader().getHash(), block1.getHeader().getPreHash());
        Assert.assertEquals(block1.getHeader().getPreHash(), block2.getHeader().getPreHash());
        Assert.assertNotEquals(block1.getHeader().getHash(), block2.getHeader().getHash());
    }
}