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

import io.nuls.network.manager.threads.*;
import io.nuls.network.model.Node;
import io.nuls.network.netty.NettyClient;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;

import java.util.concurrent.*;

/**
 * 线程任务管理
 * threads   manager
 * @author lan
 * @date 2018/11/01
 *
 */
public class TaskManager extends BaseManager{
    private static TaskManager taskManager = new TaskManager();
    private TaskManager(){

    }
    private boolean clientThreadStart = false;

    public static TaskManager getInstance(){
        if(null == taskManager){
            TaskManager taskManager = new TaskManager();
        }
        return taskManager;
    }
    private static final ExecutorService executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    /**
     * client connect thread
     * @param node
     */
    public  void doConnect(Node node) {
        ThreadUtils.createAndRunThread("doConnect",new Runnable() {
            @Override
            public void run() {
                NettyClient client = new NettyClient(node);
                client.start();
            }
        });;
    }

    @Override
    public void init() {

    }

    @Override
    public void start() {
        scheduleGroupStatusMonitor();
        TimeServiceThreadStart();
        testThread();
    }

    public void testThread(){
        //测试调试专用 开始
//            KernelThreadTest test = new KernelThreadTest();
//        test.run();
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        ScheduledThreadPoolExecutor executor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("DataShowMonitorTest"));
        executor.scheduleAtFixedRate(new DataShowMonitorTest(), 5, 10, TimeUnit.SECONDS);
        //测试调试专用 结束
    }
    public void scheduleGroupStatusMonitor(){
        ScheduledThreadPoolExecutor executor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("GroupStatusMonitor"));
        executor.scheduleAtFixedRate(new GroupStatusMonitor(), 5, 10, TimeUnit.SECONDS);
    }
    public synchronized  void clientConnectThreadStart() {
        if(clientThreadStart){
            return;
        }
        ScheduledThreadPoolExecutor executor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("NodesConnectThread"));
        executor.scheduleAtFixedRate(new NodesConnectTask(), 5, 10, TimeUnit.SECONDS);
        clientThreadStart = true;
    }

    /**
     * 启动时间同步线程
     * Start the time synchronization thread.
     */
    public void TimeServiceThreadStart() {
        Log.debug("----------- TimeService start -------------");
        TimeService timeService = TimeService.getInstance();
        timeService.initWebTimeServer();
        ThreadUtils.createAndRunThread("TimeService", timeService, true);
    }

}
