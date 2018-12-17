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

import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.block.cache.CacheHandler;
import io.nuls.block.cache.SmallBlockCacher;
import io.nuls.block.config.ConfigLoader;
import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.context.Context;
import io.nuls.block.manager.ChainManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.service.BlockService;
import io.nuls.block.test.Miner;
import io.nuls.block.thread.BlockSynchronizer;
import io.nuls.block.thread.monitor.*;
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

            onlyRunWhenTest();

            //5.rpc服务初始化
            rpcInit();

            //开启后台工作线程
            startDaemonThreads();

            while (true) {
                for (Integer chainId : ContextManager.chainIds) {
                    if (RunningStatusEnum.STOPPING.equals(ContextManager.getContext(chainId).getStatus())) {
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
        ChainManager.init(chainId);
    }

    private static void start() {
    }

    private static void init() {
    }

    /**
     * todo 正式版本删除
     */
    private static void onlyRunWhenTest() {
        Context context = ContextManager.getContext(CHAIN_ID);
        context.setStatus(RunningStatusEnum.RUNNING);
        Block latestBlock = context.getLatestBlock();
        new Miner("1", latestBlock, false).start();
        new Miner("2", latestBlock, true).start();
    }

    private static void rpcInit() throws Exception {
        // Start server instance
        WsServer.getInstance(ModuleE.BL)
                .moduleRoles(new String[]{"1.0"})
                .moduleVersion("1.0")
                .dependencies(ModuleE.KE.abbr, "1.0")
                .dependencies(ModuleE.NW.abbr, "1.0")
                .scanPackage("io.nuls.block.rpc")
                .connect("ws://192.168.1.191:8887");

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
