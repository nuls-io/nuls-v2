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

import io.nuls.base.data.BlockHeader;
import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.block.thread.BlockSynchronizer;
import io.nuls.block.thread.monitor.ChainsDbSizeMonitor;
import io.nuls.block.thread.monitor.ForkChainsMonitor;
import io.nuls.block.thread.monitor.OrphanChainsMaintainer;
import io.nuls.block.thread.monitor.OrphanChainsMonitor;
import io.nuls.block.utils.ConfigLoader;
import io.nuls.block.utils.module.NetworkUtil;
import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
import io.nuls.rpc.server.runtime.ServerRuntime;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.nuls.block.constant.Constant.*;
import static io.nuls.block.constant.Constant.CHAIN_PARAMETERS;

/**
 * 区块管理模块启动类
 * Block module startup class
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 上午10:20
 */
public class BlockBootstrap {

    /**
     * 初始化流程
     *  读取数据库,获得链ID列表,如果数据库没有配置项,加载默认配置文件
     *
     * @param args
     */
    public static void main(String[] args) {
        Thread.currentThread().setName("block-main");
        init();
        start();
        loop();
    }

    private static void loop() {
        while (true) {
            for (Integer chainId : ContextManager.chainIds) {
                ChainContext context = ContextManager.getContext(chainId);
                if (RunningStatusEnum.STOPPING.equals(context.getStatus())) {
                    System.exit(0);
                }
                BlockHeader header = context.getLatestBlock().getHeader();
                Log.info("chainId:{}, latestHeight:{}, txCount:{}, hash:{}", chainId, header.getHeight(), header.getTxCount(), header.getHash());
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            }
        }
    }

    /**
     * 系统初始化
     * 1.加载配置信息
     * 2.初始化context
     * 3.初始化bean
     * 4.初始化数据库
     * 5.初始化缓存
     * 6.初始化rpc
     * 7.初始化本地区块
     *
     * @throws Exception
     */
    private static void init() {
        try {
            //扫描包路径io.nuls.block,初始化bean
            SpringLiteContext.init(DEFAULT_SCAN_PACKAGE, new ModularServiceMethodInterceptor());
            //rpc服务初始化
            rpcInit();
            //加载配置
            ConfigLoader.load();

            RocksDBService.init(DATA_PATH);
            if (!RocksDBService.existTable(CHAIN_LATEST_HEIGHT)) {
                RocksDBService.createTable(CHAIN_LATEST_HEIGHT);
            }
            if (!RocksDBService.existTable(CHAIN_PARAMETERS)) {
                RocksDBService.createTable(CHAIN_PARAMETERS);
            }
        } catch (Exception e) {
            Log.error("error occur when init, {}", e.getMessage());
        }
    }

    /**
     * 系统正式运行
     */
    private static void start() {
        try {
            Log.info("wait depend modules ready");
            while (!ServerRuntime.isReady()) {
                Thread.sleep(100L);
            }
            NetworkUtil.register();
            Log.error("service start");
//            onlyRunWhenTest();
            //开启后台工作线程
            startDaemonThreads();
        } catch (Exception e) {
            Log.error("error occur when start, {}", e.getMessage());
        }
    }

    /**
     * todo 正式版本删除
     */
    private static void onlyRunWhenTest() {
//        ChainContext chainContext = ContextManager.getContext(chainId);
//        chainContext.setStatus(RunningStatusEnum.RUNNING);
//        Block latestBlock = chainContext.getLatestBlock();
//        new Miner("1", latestBlock).start();
//        new Miner("2", latestBlock).start();
    }

    private static void rpcInit() throws Exception {
        // Start server instance
        WsServer.getInstance(ModuleE.BL)
                .moduleRoles(new String[]{"1.0"})
                .moduleVersion("1.0")
                .dependencies(ModuleE.KE.abbr, "1.0")
                .dependencies(ModuleE.NW.abbr, "1.0")
                .scanPackage(RPC_DEFAULT_SCAN_PACKAGE)
                .connect("ws://127.0.0.1:8887");

        // Get information from kernel
        CmdDispatcher.syncKernel();

    }

    private static void startDaemonThreads() {
        //开启区块同步线程
        ScheduledThreadPoolExecutor synExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("block-synchronizer"));
        synExecutor.scheduleWithFixedDelay(BlockSynchronizer.getInstance(), 0, 10, TimeUnit.SECONDS);
//        //开启区块监控线程
//        ScheduledThreadPoolExecutor monitorExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("block-monitor"));
//        monitorExecutor.scheduleAtFixedRate(NetworkResetMonitor.getInstance(), 0, 10, TimeUnit.SECONDS);
        //开启分叉链处理线程
        ScheduledThreadPoolExecutor forkExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("fork-chains-monitor"));
        forkExecutor.scheduleWithFixedDelay(ForkChainsMonitor.getInstance(), 0, 10, TimeUnit.SECONDS);
        //开启孤儿链处理线程
        ScheduledThreadPoolExecutor orphanExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("orphan-chains-monitor"));
        orphanExecutor.scheduleWithFixedDelay(OrphanChainsMonitor.getInstance(), 0, 10, TimeUnit.SECONDS);
        //开启孤儿链维护线程
        ScheduledThreadPoolExecutor maintainExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("orphan-chains-maintainer"));
        maintainExecutor.scheduleWithFixedDelay(OrphanChainsMaintainer.getInstance(), 0, 10, TimeUnit.SECONDS);
        //开启数据库大小监控线程
        ScheduledThreadPoolExecutor dbSizeExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("db-size-monitor"));
        dbSizeExecutor.scheduleWithFixedDelay(ChainsDbSizeMonitor.getInstance(), 0, 10, TimeUnit.SECONDS);
    }

}
