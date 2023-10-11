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

package io.nuls.transaction.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: Niels Wang
 * @date: 2018/7/9
 */
public class HashSetDuplicateProcessor {

    private final int maxSize;
    private final int percent90;
    private Set<String> set1 = new HashSet<>();
    private Set<String> set2 = new HashSet<>();

    public HashSetDuplicateProcessor(int maxSize) {
        this.maxSize = maxSize;
        this.percent90 = maxSize * 9 / 10;
    }

    public synchronized boolean insertAndCheck(String hash) {
        boolean result = set1.add(hash);
        if (!result) {
            return result;
        }
        int size = set1.size();
        if (size >= maxSize) {
            set2.add(hash);
            set1.clear();
            set1.addAll(set2);
            set2.clear();
        } else if (size >= percent90) {
            set2.add(hash);
        }
        return result;
    }

    public boolean contains(String hash) {
        return set1.contains(hash);
    }

    public void remove(String hash) {
        set1.remove(hash);
        set2.remove(hash);
    }
}
