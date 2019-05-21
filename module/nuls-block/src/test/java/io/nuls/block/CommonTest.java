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

package io.nuls.block;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

public class CommonTest {

    @Test
    public void name() throws NulsException {
        {
            List<NulsDigestData> list = new ArrayList<>();
            NulsDigestData n1 = new NulsDigestData();
            n1.parse(new NulsByteBuffer("00205a1df0c7633cab1f457397e7a8d80432d989253376d2123f5ad9189384089d7d".getBytes()));
            list.add(n1);
            NulsDigestData n2 = new NulsDigestData();
            n2.parse(new NulsByteBuffer("0020103f2a6285c17e9c2d18688376315e46d60a2d2613ac3a23f91cada3c4671a2c".getBytes()));
            list.add(n2);
            String m1 = NulsDigestData.calcMerkleDigestData(list).toString();
            System.out.println(m1);
        }
        {
            List<NulsDigestData> list = new ArrayList<>();
            NulsDigestData n1 = new NulsDigestData();
            n1.parse(new NulsByteBuffer("0020103f2a6285c17e9c2d18688376315e46d60a2d2613ac3a23f91cada3c4671a2c".getBytes()));
            list.add(n1);
            NulsDigestData n2 = new NulsDigestData();
            n2.parse(new NulsByteBuffer("00205a1df0c7633cab1f457397e7a8d80432d989253376d2123f5ad9189384089d7d".getBytes()));
            list.add(n2);
            String m1 = NulsDigestData.calcMerkleDigestData(list).toString();
            System.out.println(m1);
        }
    }

    @Test
    public void test1() {
        Map map = Collections.synchronizedMap(new LinkedHashMap<Integer, String>(100) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                return size() > 100;
            }
        });
        long start = System.currentTimeMillis();
        for (int i = 0; i < 20000000; i++) {
            map.put(i, "hello" + i);
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        System.out.println(map.size());
    }

    @Test
    public void test2() {
        Map map = new LinkedHashMap<Integer, String>(100) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                return size() > 100;
            }
        };
        long start = System.currentTimeMillis();
        for (int i = 0; i < 20000000; i++) {
            map.put(i, "hello" + i);
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        System.out.println(map.size());
    }

    @Test
    public void test3() {
        Map map = CollectionUtils.getSizedMap(100);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 20000000; i++) {
            map.put(i, "hello" + i);
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        System.out.println(map.size());
    }

    @Test
    public void thenApply() {
        String result = CompletableFuture.supplyAsync(() -> "hello").thenApply(s -> s + " world").join();
        System.out.println(result);
    }

    @Test
    public void thenAccept(){
        CompletableFuture.supplyAsync(() -> "hello").thenAccept(s -> System.out.println(s+" world"));
    }

    @Test
    public void testLock1() {
        StampedLock lock = new StampedLock();
        long lock1 = lock.writeLock();
        lock.unlockWrite(lock1);

        long lock2 = lock.writeLock();
        lock.unlockWrite(lock2);

        long lock3 = lock.writeLock();
        lock.unlockWrite(lock3);
    }

    @Test
    public void atomicInteger() {
        AtomicInteger max = new AtomicInteger(Integer.MAX_VALUE);
        System.out.println(max);
        System.out.println(max.incrementAndGet());
        System.out.println("--------------------");
        AtomicInteger min = new AtomicInteger(Integer.MIN_VALUE);
        System.out.println(min);
        System.out.println(min.decrementAndGet());
    }

    @Test
    public void testLinkedListClone() {
        LinkedList<String> list = new LinkedList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        LinkedList<String> clone = (LinkedList<String>) list.clone();
        clone.pop();
        clone.pop();
        clone.pop();
        Assert.assertEquals(1, clone.size());
        Assert.assertEquals(4, list.size());
    }

    @Test
    public void testAtomicInteger() {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        atomicInteger.addAndGet(-999);
        System.out.println(atomicInteger);
    }
}