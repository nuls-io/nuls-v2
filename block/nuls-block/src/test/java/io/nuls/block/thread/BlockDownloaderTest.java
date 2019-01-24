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

package io.nuls.block.thread;

import io.nuls.block.model.Node;

import static io.nuls.block.constant.Constant.NODE_COMPARATOR;
import static io.nuls.block.utils.LoggerUtil.commonLog;
import org.junit.Test;

import java.util.concurrent.PriorityBlockingQueue;

import static org.junit.Assert.assertEquals;

public class BlockDownloaderTest {

    @Test
    public void test2() {
        long netLatestHeight = 158;
        long startHeight = 1;

        while (startHeight <= netLatestHeight) {
            int size = 20;
            if (startHeight + size > netLatestHeight) {
                size = (int) (netLatestHeight - startHeight + 1);
            }
            commonLog.info("get blocks "+startHeight+"->"+(startHeight + size - 1));
            startHeight += size;
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

    @Test
    public void test3() throws InterruptedException {
        var queue = new PriorityBlockingQueue<Node>(3, NODE_COMPARATOR);
        Node node1 = new Node();
        node1.setCredit(100);
        queue.add(node1);

        Node node2 = new Node();
        node2.setCredit(90);
        queue.add(node2);

        Node node3 = new Node();
        node3.setCredit(110);
        queue.add(node3);

        assertEquals(110, queue.take().getCredit());
        assertEquals(100, queue.take().getCredit());
        assertEquals(90, queue.take().getCredit());
    }

    @Test
    public void name() {

        int size = 20 * 100 / 120;
    }
}