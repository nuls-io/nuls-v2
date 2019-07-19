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

import io.nuls.core.log.Log;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;
import io.nuls.network.constant.ManagerStatusEnum;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.task.*;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程任务管理
 * threads   manager
 *
 * @author lan
 * @date 2018/11/01
 */
public class TaskManager extends BaseManager {
    private static TaskManager taskManager = new TaskManager();
    private ScheduledThreadPoolExecutor executorService;

    private TaskManager() {

    }

    private boolean clientThreadStart = false;

    public static TaskManager getInstance() {
        if (null == taskManager) {
            taskManager = new TaskManager();
        }
        return taskManager;
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void start() throws Exception {
        executorService = ThreadUtils.createScheduledThreadPool(6, new NulsThreadFactory("NetWorkThread"));
        connectTasks();
        scheduleGroupStatusMonitor();
        timeServiceThreadStart();
        nwInfosThread();
        peerCacheMsgSendTask();
        heartBeatThread();
    }

    @Override
    public void change(ManagerStatusEnum toStatus) throws Exception {

    }

    private void connectTasks() {
        executorService.scheduleWithFixedDelay(new NodeMaintenanceTask(), 1, 5, TimeUnit.SECONDS);
        executorService.scheduleWithFixedDelay(new SaveNodeInfoTask(), 1, 1, TimeUnit.MINUTES);
        executorService.scheduleWithFixedDelay(new NodeDiscoverTask(), 3, 10, TimeUnit.SECONDS);
    }

    private void nwInfosThread() {
        executorService.scheduleWithFixedDelay(new NwInfosPrintTask(), 5, 60, TimeUnit.SECONDS);
    }


    private void heartBeatThread() {
        executorService.scheduleWithFixedDelay(new HeartBeatTask(), 5, 25, TimeUnit.SECONDS);
    }

    private void scheduleGroupStatusMonitor() {
        executorService.scheduleWithFixedDelay(new GroupStatusMonitor(), 5, 10, TimeUnit.SECONDS);
    }

    /**
     * 启动时间同步线程
     * Start the time synchronization thread.
     */
    private void timeServiceThreadStart() {
        Log.debug("----------- TimeService start -------------");
        TimeManager.getInstance().initWebTimeServer();
        ThreadUtils.createAndRunThread("TimeTask", new TimeTask(), true);
    }

    private void peerCacheMsgSendTask() {
        Log.debug("----------- peerCacheMsgSendTask start -------------");
        ThreadUtils.createAndRunThread("peerCacheMsgSendTask", new PeerCacheMsgSendTask(), true);
    }

    public void createShareAddressTask(NodeGroup nodeGroup, boolean isCross) {
        Log.debug("----------- createShareAddressTask start -------------");
        ThreadUtils.createAndRunThread("share-mine-node", new ShareAddressTask(nodeGroup, isCross));
    }
}
