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

package io.nuls.block.model;

import io.nuls.base.data.NulsHash;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.SortedSet;

public class ChainTest {

    /**
     * 主要测试Chain的比较排序
     */
    @Test
    public void testComparator() {
        int count = 0;
        Chain father = new Chain();
        ArrayDeque<NulsHash> hash = new ArrayDeque<>();
        hash.add(NulsHash.calcHash(("chain" + count++).getBytes()));
        father.setHashList(hash);
        father.setStartHashCode(hash.getFirst().hashCode());
        father.setStartHeight(1000);
        father.setEndHeight(2000);

        Chain son1 = new Chain();
        ArrayDeque<NulsHash> hash1 = new ArrayDeque<>();
        hash1.add(NulsHash.calcHash(("chain" + count++).getBytes()));
        son1.setHashList(hash1);
        son1.setStartHashCode(hash1.getFirst().hashCode());
        son1.setStartHeight(1300);
        son1.setEndHeight(1600);

        Chain son2 = new Chain();
        ArrayDeque<NulsHash> hash2 = new ArrayDeque<>();
        hash2.add(NulsHash.calcHash(("chain" + count++).getBytes()));
        son2.setHashList(hash2);
        son2.setStartHashCode(hash2.getFirst().hashCode());
        son2.setStartHeight(1400);
        son2.setEndHeight(1500);

        Chain son3 = new Chain();
        ArrayDeque<NulsHash> hash3 = new ArrayDeque<>();
        hash3.add(NulsHash.calcHash(("chain" + count++).getBytes()));
        son3.setHashList(hash3);
        son3.setStartHashCode(hash3.getFirst().hashCode());
        son3.setStartHeight(1100);
        son3.setEndHeight(2200);

        Chain son4 = new Chain();
        ArrayDeque<NulsHash> hash4 = new ArrayDeque<>();
        hash4.add(NulsHash.calcHash(("chain" + count++).getBytes()));
        son4.setHashList(hash4);
        son4.setStartHashCode(hash4.getFirst().hashCode());
        son4.setStartHeight(1200);
        son4.setEndHeight(1800);

        Chain son5 = new Chain();
        ArrayDeque<NulsHash> hash5 = new ArrayDeque<>();
        hash5.add(NulsHash.calcHash(("chain" + count++).getBytes()));
        son5.setHashList(hash5);
        son5.setStartHashCode(hash5.getFirst().hashCode());
        son5.setStartHeight(1600);
        son5.setEndHeight(1900);
        System.out.println(son5.getStartHashCode());

        Chain son6 = new Chain();
        ArrayDeque<NulsHash> hash6 = new ArrayDeque<>();
        hash6.add(NulsHash.calcHash(("chain" + count++).getBytes()));
        son6.setHashList(hash6);
        son6.setStartHashCode(hash6.getFirst().hashCode());
        son6.setStartHeight(1600);
        son6.setEndHeight(1800);
        System.out.println(son6.getStartHashCode());

        father.getSons().add(son1);
        father.getSons().add(son2);
        father.getSons().add(son3);
        father.getSons().add(son4);
        father.getSons().add(son5);
        father.getSons().add(son6);

        Assert.assertEquals(father.getSons().first().getStartHeight(), 1100);
        Assert.assertEquals(father.getSons().last().getStartHeight(), 1600);
        Assert.assertEquals(1, father.getSons().headSet(son4).size());
        Assert.assertEquals(3, father.getSons().headSet(son2).size());
        Assert.assertEquals(5, father.getSons().headSet(son5).size());
        Assert.assertEquals(1, father.getSons().tailSet(son5).size());

        SortedSet<Chain> chains = father.getSons().tailSet(son5);
        System.out.println(chains);


    }

}