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

package io.nuls.block.thread;

import io.nuls.tools.log.Log;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BlockDownloaderManagerTest {

    @Test
    public void test2() {
        int nodeCount = 10;
        int maxDowncount = 10;
        long latestHeight = 999;
        long netLatestHeight = 2000;
        long totalCount = netLatestHeight - latestHeight;
        int roundDownloads = maxDowncount * nodeCount;
        //需要下载多少轮
        long round = (long) Math.ceil((double) totalCount / (roundDownloads));
        for (long i = 0; i < round; i++) {
            long startHeight = (latestHeight + 1) + i * roundDownloads;
            for (int j = 0; j < nodeCount; j++) {
                long start = startHeight + j * maxDowncount;
                int size = maxDowncount;

                //最后一个节点的下载区块数，特殊计算
                boolean isEnd = false;
                if (start + size > netLatestHeight) {
                    size = (int) (netLatestHeight - start) + 1;
                    isEnd = true;
                }

                Log.info("round{} download {}->{}, form node{}", i+1, start, start+size-1, j);
                if (isEnd) {
                    break;
                }
            }
        }
    }

    @Test
    public void test1() {
        {
            long totalCount = 1000;
            int roundDownloads = 100;
            //需要下载多少轮
            long round = (long) Math.ceil((double) totalCount / (roundDownloads));
            assertEquals(10, round);
        }

        {
            long totalCount = 1001;
            int roundDownloads = 100;
            //需要下载多少轮
            long round = (long) Math.ceil((double) totalCount / (roundDownloads));
            assertEquals(11, round);
        }
    }
}