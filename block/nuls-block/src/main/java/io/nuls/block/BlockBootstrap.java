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
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.nuls.block.constant.Constant.*;
import static io.nuls.block.utils.LoggerUtil.commonLog;

/**
 * 区块管理模块启动类
 * Block module startup class
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 上午10:20
 */
public class BlockBootstrap {

    public static void main(String[] args) {
        Thread.currentThread().setName("block-main");
        init();
        start();
        loop();
    }

    private static void init() {
        try {
            //扫描包路径io.nuls.block,初始化bean
            SpringLiteContext.init(DEFAULT_SCAN_PACKAGE);
            //rpc服务初始化
            WsServer.getInstance(ModuleE.BL)
                    .moduleRoles(new String[]{"1.0"})
                    .moduleVersion("1.0")
                    .dependencies(ModuleE.KE.abbr, "1.0")
                    .dependencies(ModuleE.CS.abbr, "1.0")
                    .dependencies(ModuleE.NW.abbr, "1.0")
                    .dependencies(ModuleE.TX.abbr, "1.0")
                    .scanPackage(RPC_DEFAULT_SCAN_PACKAGE)
                    .connect("ws://localhost:8887");
            // Get information from kernel
            CmdDispatcher.syncKernel();
            //加载通用数据库
            RocksDBService.init(DATA_PATH);
            RocksDBService.createTable(CHAIN_LATEST_HEIGHT);
            RocksDBService.createTable(CHAIN_PARAMETERS);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error("error occur when init, " + e.getMessage());
        }
    }

    private static void start() {
        try {
            while (!ServerRuntime.isReady()) {
                commonLog.info("wait depend modules ready");
                Thread.sleep(2000L);
            }
            NetworkUtil.register();
            commonLog.info("service start");
            //加载配置
            ConfigLoader.load();
//            onlyRunWhenTest();

            //开启区块同步线程
            ThreadUtils.createAndRunThread("block-synchronizer", BlockSynchronizer.getInstance());
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
            maintainExecutor.scheduleWithFixedDelay(OrphanChainsMaintainer.getInstance(), 0, 5, TimeUnit.SECONDS);
            //开启数据库大小监控线程
            ScheduledThreadPoolExecutor dbSizeExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("db-size-monitor"));
            dbSizeExecutor.scheduleWithFixedDelay(ChainsDbSizeMonitor.getInstance(), 0, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error("error occur when start, " + e.getMessage());
        }
    }

    private static void loop() {
        while (true) {
            for (Integer chainId : ContextManager.chainIds) {
                ChainContext context = ContextManager.getContext(chainId);
                if (RunningStatusEnum.STOPPING.equals(context.getStatus())) {
                    System.exit(0);
                }
                BlockHeader header = context.getLatestBlock().getHeader();
                commonLog.info("chainId:" + chainId + ", latestHeight:" + header.getHeight() + ", txCount:" + header.getTxCount() + ", hash:" + header.getHash());
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    commonLog.error(e);
                }
            }
        }
    }

    /**
     * todo 正式版本删除
     */
    public static void onlyRunWhenTest() {
        ContextManager.chainIds.forEach(e -> {
            try {
                RocksDBService.createTable("tx" + e);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
//        ChainContext chainContext = ContextManager.getContext(chainId);
//        chainContext.setStatus(RunningStatusEnum.RUNNING);
//        Block latestBlock = chainContext.getLatestBlock();
//        new Miner("1", latestBlock).start();
//        new Miner("2", latestBlock).start();
    }
}
