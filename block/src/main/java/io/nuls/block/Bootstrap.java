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
import io.nuls.base.data.Block;
import io.nuls.block.config.ConfigLoader;
import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.CoinBaseTransaction;
import io.nuls.block.service.BlockService;
import io.nuls.block.thread.BlockSynchronizer;
import io.nuls.block.thread.ShutdownHook;
import io.nuls.block.thread.monitor.ForkChainsMonitor;
import io.nuls.block.thread.monitor.NetworkResetMonitor;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.TimeService;
import io.nuls.tools.thread.commom.NulsThreadFactory;
import org.apache.commons.httpclient.util.DateUtil;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.nuls.block.constant.Constant.CHAIN_ID;
import static io.nuls.block.constant.Constant.MODULES_CONFIG_FILE;

/**
 * 区块管理模块启动类
 * Block module startup class
 * @author captain
 * @date 18-11-8 上午10:20
 * @version 1.0
 */
public class Bootstrap {

    public static void main(String[] args) {
        Thread.currentThread().setName("block-bootstrap");
        try {
            init();

            start();

            TimeService.getInstance().start();
            //1.加载配置
            ConfigLoader.load(MODULES_CONFIG_FILE);
            //2.加载Context
            ContextManager.init(CHAIN_ID);
            int chainId = CHAIN_ID;

            //3.扫描包路径io.nuls.block，初始化bean
            SpringLiteContext.init("io.nuls.block", new ModularServiceMethodInterceptor());

            //4.服务初始化
            BlockService service = ContextManager.getServiceBean(BlockService.class);
            service.init(chainId);

            //交易注册，用于创世块
            TransactionManager.putTx(CoinBaseTransaction.class, null);

            //5.rpc服务初始化
            rpcInit();

            onlyRunWhenTest();

            //开启后台工作线程
            startDaemonThreads();

            while (true) {
                if (RunningStatusEnum.STOPPING.equals(ContextManager.getContext(chainId).getStatus())) {
                    Runtime.getRuntime().addShutdownHook(new ShutdownHook());
                    System.exit(0);
                }
                Log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-  netTime : " + (DateUtil.formatDate(new Date(TimeService.currentTimeMillis()))));
                Block bestBlock = ContextManager.getContext(chainId).getLatestBlock();
                Log.info("latestHeight:{} , txCount:{} , hash : {}", bestBlock.getHeader().getHeight(), bestBlock.getHeader().getTxCount(), bestBlock.getHeader().getHash());
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            }

        } catch (Exception e) {
            Log.error(e);
        }
    }

    private static void start() {
    }

    private static void init() {
    }

    private static void onlyRunWhenTest() {
        new BlockGenerator().start();
    }

    private static void rpcInit() throws Exception {
        // 启动Server
        WsServer.getInstance(ModuleE.BL).setScanPackage("io.nuls.block.rpc").connect("ws://127.0.0.1:8887");
    }

    private static void startDaemonThreads() {
        //开启区块同步线程
        ScheduledThreadPoolExecutor synExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("block-synchronize"));
        synExecutor.scheduleAtFixedRate(BlockSynchronizer.getInstance(), 0, 1, TimeUnit.MINUTES);
        //开启区块监控线程
        ScheduledThreadPoolExecutor monitorExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("block-monitor"));
        monitorExecutor.scheduleAtFixedRate(NetworkResetMonitor.getInstance(), 0, 1, TimeUnit.MINUTES);
        //开启分叉链处理线程
        ScheduledThreadPoolExecutor forkExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("fork-chains-monitor"));
        forkExecutor.scheduleAtFixedRate(ForkChainsMonitor.getInstance(), 0, 1, TimeUnit.MINUTES);
    }

}
