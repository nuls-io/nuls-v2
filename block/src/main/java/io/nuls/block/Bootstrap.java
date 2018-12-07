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

package io.nuls.block;

import io.nuls.base.basic.TransactionManager;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.block.cache.CacheHandler;
import io.nuls.block.cache.SmallBlockCacher;
import io.nuls.block.config.ConfigLoader;
import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.service.BlockService;
import io.nuls.block.thread.BlockSynchronizer;
import io.nuls.block.thread.ShutdownHook;
import io.nuls.block.thread.monitor.ChainsDbSizeMonitor;
import io.nuls.block.thread.monitor.ForkChainsMonitor;
import io.nuls.block.thread.monitor.NetworkResetMonitor;
import io.nuls.block.thread.monitor.OrphanChainsMonitor;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.TimeService;
import io.nuls.tools.thread.commom.NulsThreadFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.nuls.block.constant.Constant.CHAIN_ID;
import static io.nuls.block.constant.Constant.MODULES_CONFIG_FILE;

/**
 * 区块管理模块启动类
 * Block module startup class
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 上午10:20
 */
public class Bootstrap {

    public static void main(String[] args) {
        Thread.currentThread().setName("block-main");
        try {
            init();

            start();

            TimeService.getInstance().start();
            //1.加载配置
            ConfigLoader.load(MODULES_CONFIG_FILE);
            //2.加载Context
            ContextManager.init(CHAIN_ID);

            //3.扫描包路径io.nuls.block，初始化bean
            SpringLiteContext.init("io.nuls.block", new ModularServiceMethodInterceptor());

            //4.服务初始化
            BlockService service = ContextManager.getServiceBean(BlockService.class);
            service.init(CHAIN_ID);

            //各类缓存初始化
            initCache(CHAIN_ID);

            //5.rpc服务初始化
            rpcInit();

            onlyRunWhenTest();

            //开启后台工作线程
            startDaemonThreads();

            while (true) {
                for (Integer chainId : ContextManager.chainIds) {
                    if (RunningStatusEnum.STOPPING.equals(ContextManager.getContext(chainId).getStatus())) {
                        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
                        System.exit(0);
                    }
                    BlockHeader header = ContextManager.getContext(chainId).getLatestBlock().getHeader();
                    Log.info("chainId:{}, latestHeight:{}, txCount:{}, hash:{}", chainId, header.getHeight(), header.getTxCount(), header.getHash());
                    try {
                        Thread.sleep(10000L);
                    } catch (InterruptedException e) {
                        Log.error(e);
                    }
                }
            }

        } catch (Exception e) {
            Log.error(e);
        }
    }

    private static void initCache(int chainId) {
        SmallBlockCacher.init(chainId);
        CacheHandler.init(chainId);
    }

    private static void start() {
    }

    private static void init() {
    }

    /**
     * todo 正式版本删除
     */
    private static void onlyRunWhenTest() {
        ContextManager.getContext(CHAIN_ID).setStatus(RunningStatusEnum.RUNNING);
        new BlockGenerator().start();
    }

    private static void rpcInit() throws Exception {
        // Start server instance
        WsServer.getInstance(ModuleE.BL)
                .moduleRoles(new String[]{"1.0"})
                .moduleVersion("1.0")
                .dependencies(ModuleE.KE.abbr, "1.0")
                .dependencies(ModuleE.NW.abbr, "1.0")
                .scanPackage("io.nuls.block.rpc")
                .connect("ws://127.0.0.1:8887");

        // Get information from kernel
        CmdDispatcher.syncKernel();
    }

    private static void startDaemonThreads() {
        //开启区块同步线程
        ScheduledThreadPoolExecutor synExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("block-synchronize"));
        synExecutor.scheduleAtFixedRate(BlockSynchronizer.getInstance(), 0, 1, TimeUnit.MINUTES);
        //开启区块监控线程
//        ScheduledThreadPoolExecutor monitorExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("block-monitor"));
//        monitorExecutor.scheduleAtFixedRate(NetworkResetMonitor.getInstance(), 0, 1, TimeUnit.MINUTES);
        //开启分叉链处理线程
        ScheduledThreadPoolExecutor forkExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("fork-chains-monitor"));
        forkExecutor.scheduleAtFixedRate(ForkChainsMonitor.getInstance(), 0, 1, TimeUnit.MINUTES);
        //开启孤儿链处理线程
        ScheduledThreadPoolExecutor orphanExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("orphan-chains-monitor"));
        orphanExecutor.scheduleAtFixedRate(OrphanChainsMonitor.getInstance(), 0, 1, TimeUnit.MINUTES);
        //开启数据库大小监控线程
        ScheduledThreadPoolExecutor dbSizeExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("DBSize-monitor"));
        dbSizeExecutor.scheduleAtFixedRate(ChainsDbSizeMonitor.getInstance(), 0, 10, TimeUnit.SECONDS);
    }

}
