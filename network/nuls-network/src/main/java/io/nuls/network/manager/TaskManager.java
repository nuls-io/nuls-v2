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
package io.nuls.network.manager;

import io.nuls.tools.core.aop.AopUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;
import io.nuls.tools.thread.commom.ThreadPoolInterceiptor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
/**
 * 线程任务管理
 * threads   manager
 * @author lan
 * @date 2018/11/01
 *
 */
public class TaskManager {


    public static final void createAndRunThread(String threadName, Runnable runnable, boolean deamon) {

        NulsThreadFactory factory = new NulsThreadFactory(threadName);
        Thread thread = new Thread(runnable);
        thread.setDaemon(deamon);
        thread.start();
    }
    public static final ScheduledThreadPoolExecutor createScheduledThreadPool(int threadCount, NulsThreadFactory factory) {
        if (factory == null) {
            throw new RuntimeException("thread factory cannot be null!");
        }
        ScheduledThreadPoolExecutor pool = AopUtils.createProxy(ScheduledThreadPoolExecutor.class, new Class[]{int.class, ThreadFactory.class}, new Object[]{threadCount, factory}, new ThreadPoolInterceiptor());
        return pool;
    }

}
