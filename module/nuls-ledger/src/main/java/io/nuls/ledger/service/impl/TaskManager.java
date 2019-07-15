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
package io.nuls.ledger.service.impl;

import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;
import io.nuls.ledger.task.LedgerBlockSyncTask;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程任务管理
 * threads   manager
 *
 * @author lan
 * @date 2018/11/01
 */
public class TaskManager {
    private static TaskManager taskManager = new TaskManager();
    private ScheduledThreadPoolExecutor executorService;

    private TaskManager() {

    }


    public static TaskManager getInstance() {
        if (null == taskManager) {
            taskManager = new TaskManager();
        }
        return taskManager;
    }

    public void start() {
        executorService = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("LedgerThread"));
        syncBlockInfos();
    }

    private void syncBlockInfos() {
//        executorService.scheduleWithFixedDelay(new LedgerBlockSyncTask(), 1, 5, TimeUnit.SECONDS);
    }

}
