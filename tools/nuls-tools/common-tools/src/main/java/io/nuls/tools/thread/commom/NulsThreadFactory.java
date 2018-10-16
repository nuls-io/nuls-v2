/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.tools.thread.commom;

import io.nuls.tools.thread.ThreadUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Niels
 */
public class NulsThreadFactory implements ThreadFactory {
    private final String poolName;
    private AtomicInteger threadNo = new AtomicInteger(1);

    /**
     * NulsThreadFactory构造函数
     *
     * @param poolName 线程工厂名称（用于生成以后用该对象生成出来的线程的名称）
     */
    public NulsThreadFactory(String poolName) {
        this.poolName = poolName;
    }

    /**
     * 创建一个线程
     *
     * @param r Runnable
     * @return Thread
     */
    @Override
    public final Thread newThread(Runnable r) {
        String threadName;
        if (threadNo.get() == 1) {
            threadNo.incrementAndGet();
            threadName = "[" + poolName + "]";
        } else {
            threadName = "[" + poolName + "-" + threadNo.getAndIncrement() + "]";
        }
        Thread newThread = new Thread(r, threadName);
        newThread.setDaemon(true);
        if (newThread.getPriority() != Thread.NORM_PRIORITY) {
            newThread.setPriority(Thread.NORM_PRIORITY);
        }
        ThreadUtils.putThread(poolName, threadName, newThread);
        return newThread;
    }

    /**
     * 获取当前线程工厂名称
     */
    public String getPoolName() {
        return poolName;
    }
}
