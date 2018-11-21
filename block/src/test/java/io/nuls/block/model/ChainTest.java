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

package io.nuls.block.model;

import io.nuls.base.data.NulsDigestData;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.assertTrue;

public class ChainTest {

    @Test
    public void testMerge() {
    }

    @Test
    public void test() {
        Chain father = new Chain();
        father.setStartHeight(1000);
        father.setEndHeight(2000);

        Chain son1 = new Chain();
        son1.setStartHeight(1300);
        son1.setEndHeight(1600);

        Chain son2 = new Chain();
        son2.setStartHeight(1400);
        son2.setEndHeight(1500);

        Chain son3 = new Chain();
        son3.setStartHeight(1100);
        son3.setEndHeight(2200);

        Chain son4 = new Chain();
        son4.setStartHeight(1200);
        son4.setEndHeight(1800);

        Chain son5 = new Chain();
        son5.setStartHeight(1600);
        son5.setEndHeight(1900);

        father.getSons().add(son1);
        father.getSons().add(son2);
        father.getSons().add(son3);
        father.getSons().add(son4);
        father.getSons().add(son5);

        Assert.assertEquals(father.getSons().first().getStartHeight(), 1100);
        Assert.assertEquals(father.getSons().last().getStartHeight(), 1600);
        Assert.assertEquals(father.getSons().headSet(son4).size(), 1);
        Assert.assertEquals(father.getSons().headSet(son2).size(), 3);

    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        Chain master = new Chain();
        LinkedList<NulsDigestData> hashList = new LinkedList<>();
        hashList.add(NulsDigestData.calcDigestData("hello".getBytes()));
        master.setHashList(hashList);
        master.setChainId(8998);
        master.setStartHeight(0);
        master.setEndHeight(1000);
        master.setMaster(true);
        master.setParent(null);
        master.setPreviousHash(NulsDigestData.calcDigestData("hello".getBytes()));

        Chain chainA = new Chain();
        chainA.setHashList(hashList);
        chainA.setChainId(8998);
        chainA.setStartHeight(800);
        chainA.setEndHeight(900);
        chainA.setMaster(false);
        chainA.setParent(master);
        chainA.setPreviousHash(NulsDigestData.calcDigestData("hello".getBytes()));

        Chain chainB = new Chain();
        chainB.setHashList(hashList);
        chainB.setChainId(8998);
        chainB.setStartHeight(850);
        chainB.setEndHeight(880);
        chainB.setMaster(false);
        chainB.setParent(chainA);
        chainB.setPreviousHash(NulsDigestData.calcDigestData("hello".getBytes()));

        Chain temp = chainB.clone();
        temp.setParent(null);

        assertTrue(chainB.getParent() != null);
    }

}