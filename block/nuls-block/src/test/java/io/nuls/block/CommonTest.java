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

import io.nuls.block.constant.ChainTypeEnum;
import io.nuls.block.model.Chain;
import io.nuls.block.model.Node;
import io.nuls.tools.cache.LimitHashMap;
import io.nuls.tools.data.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

import static io.nuls.block.constant.Constant.NODE_COMPARATOR;

public class CommonTest {

    @Test
    public void test() {
        LimitHashMap map = new LimitHashMap(100);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 20000000; i++) {
            map.put(i, "hello" + i);
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        System.out.println(map.size());
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
    public void testLock() {
        for (int i = 0; i < 10; i++) {
            try {
                if (true) {
                    System.out.println("22222222222222");
                    throw new RuntimeException();
                }
                System.out.println("888888888");
            } finally {
                System.out.println("1111111111111");
            }
        }
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
    public void test4() {
        TreeSet<Chain> chains = new TreeSet<>(Chain.COMPARATOR);
        chains.forEach(e -> e.setType(ChainTypeEnum.FORK));
        SortedSet<Chain> ss = Collections.emptySortedSet();
        ss.forEach(e -> e.setType(ChainTypeEnum.FORK));
    }

    @Test
    public void test5() {
        {
            SortedSet<Node> nodes = new TreeSet<>(NODE_COMPARATOR);
            Random random = new Random();
            long l = System.currentTimeMillis();
            for (int i = 0; i < 10000000; i++) {
                nodes.add(getNode(random.nextInt()));
            }
            System.out.println("SortedSet-"+(System.currentTimeMillis() - l));
        }
        {
            List<Node> nodes = new ArrayList<>();
            Random random = new Random();
            long l = System.currentTimeMillis();
            for (int i = 0; i < 10000000; i++) {
                nodes.add(getNode(random.nextInt()));
            }
            nodes.sort(NODE_COMPARATOR);
            System.out.println("List-"+(System.currentTimeMillis() - l));
        }
    }

    private Node getNode(int credit){
        Node node = new Node();
        node.setCredit(credit);
        return node;
    }
}