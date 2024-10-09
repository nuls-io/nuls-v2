/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.crosschain.base.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: Niels Wang
 * @date: 2018/7/9
 */
public class HashSetTimeDuplicateProcessor {

    private static final long allowMs = 60000;

    private final int maxSize;
    private final int percent90;
    private final long timeoutMs;
    private Map<String, Long> map1 = new HashMap<>();
    private Map<String, Long> map2 = new HashMap<>();

    public HashSetTimeDuplicateProcessor(int maxSize, long ms) {
        this.maxSize = maxSize;
        this.percent90 = maxSize * 9 / 10;
        this.timeoutMs = ms;
    }

    public synchronized boolean insertAndCheck(String hash) {
        boolean result = map1.containsKey(hash);
        if (!result) {
            map1.put(hash, System.currentTimeMillis());
            return true;
        }
        Long start = map1.get(hash);
        long sub = System.currentTimeMillis() - start;
        long timeVal = System.currentTimeMillis();
        if (sub >= timeoutMs || sub < allowMs) {
            result = true;
        } else {
            timeVal = start;
            result = false;
        }
        map1.put(hash, timeVal);
        int size = map1.size();
        if (size >= maxSize) {
            map2.put(hash, timeVal);
            map1.clear();
            map1.putAll(map2);
            map2.clear();
        } else if (size >= percent90) {
            map2.put(hash, timeVal);
        }
        return result;
    }

    public boolean contains(String hash) {
        return map1.containsKey(hash);
    }

    public void remove(String hash) {
        map1.remove(hash);
        map2.remove(hash);
    }

    public static void main(String[] args) {
        HashSetTimeDuplicateProcessor processor = new HashSetTimeDuplicateProcessor(1000,100000L);
        assertTrueTest(processor.insertAndCheck("1"));
        assertTrueTest(processor.insertAndCheck("2"));
        assertTrueTest(processor.insertAndCheck("3"));

        assertTrueTest(processor.insertAndCheck("1"));
        assertTrueTest(processor.insertAndCheck("2"));

        try {
            Thread.sleep(60000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertFlaseTest(processor.insertAndCheck("1"));
        assertFlaseTest(processor.insertAndCheck("2"));

        try {
            Thread.sleep(100000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertTrueTest(processor.insertAndCheck("1"));
        assertTrueTest(processor.insertAndCheck("2"));
        assertTrueTest(processor.insertAndCheck("3"));

        assertTrueTest(processor.insertAndCheck("1"));
        assertTrueTest(processor.insertAndCheck("2"));
        try {
            Thread.sleep(60000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertFlaseTest(processor.insertAndCheck("1"));
        assertFlaseTest(processor.insertAndCheck("2"));
        System.out.println("Success!!");
    }

    private static void assertTrueTest(boolean b) {
        if(!b){
            throw new RuntimeException("预期为true，实际得到false");
        }
    }

    private static void assertFlaseTest(boolean b) {
        if(b){
            throw new RuntimeException("预期为false，实际得到true");
        }
    }
}
