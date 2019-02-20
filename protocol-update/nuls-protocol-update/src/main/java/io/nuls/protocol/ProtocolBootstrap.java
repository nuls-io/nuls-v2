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

package io.nuls.protocol;

import io.nuls.db.service.RocksDBService;
import io.nuls.protocol.constant.RunningStatusEnum;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.model.ProtocolVersion;
import io.nuls.protocol.model.po.Statistics;
import io.nuls.protocol.thread.monitor.ProtocolMonitor;
import io.nuls.protocol.utils.ConfigLoader;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
import io.nuls.rpc.server.runtime.ServerRuntime;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.nuls.protocol.constant.Constant.*;
import static io.nuls.protocol.utils.LoggerUtil.commonLog;

/**
 * 协议升级模块启动类
 * protocol update module startup class
 *
 * @author captain
 * @version 1.0
 * @date 19-1-25 上午10:48
 */
public class ProtocolBootstrap {

    public static void main(String[] args) {
        Thread.currentThread().setName("protocol-main");
        init();
        start();
        loop();
    }

    /**
     * 初始化，完成后系统状态变更为{@link RunningStatusEnum#READY}
     */
    private static void init() {
        try {
            //扫描包路径io.nuls.protocol,初始化bean
            SpringLiteContext.init(DEFAULT_SCAN_PACKAGE);
            //rpc服务初始化
            WsServer.getInstance(ModuleE.PU)
                    .moduleRoles(new String[]{"1.0"})
                    .moduleVersion("1.0")
                    .dependencies(ModuleE.KE.abbr, "1.0")
                    .scanPackage(RPC_DEFAULT_SCAN_PACKAGE)
                    .connect("ws://localhost:8887");
            // Get information from kernel
            CmdDispatcher.syncKernel();
            //加载通用数据库
            RocksDBService.init(DATA_PATH);
            RocksDBService.createTable(PROTOCOL_CONFIG);
            //加载配置
            ConfigLoader.load();
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
            commonLog.info("service starting");
            //开启分叉链处理线程
            ScheduledThreadPoolExecutor forkExecutor = ThreadUtils.createScheduledThreadPool(1, new NulsThreadFactory("fork-chains-monitor"));
            forkExecutor.scheduleWithFixedDelay(ProtocolMonitor.getInstance(), 0, 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error("error occur when start, " + e.getMessage());
        }
    }

    private static void loop() {
        while (true) {
            for (Integer chainId : ContextManager.chainIds) {
                ProtocolContext context = ContextManager.getContext(chainId);
                if (RunningStatusEnum.FAIL.equals(context.getStatus())) {
                    System.exit(0);
                }
                ProtocolVersion protocolVersion = context.getCurrentProtocolVersion();
                commonLog.info("chainId:" + chainId + ", protocaolVersion:" + protocolVersion.getVersion());
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    commonLog.error(e);
                }
            }
        }
    }

}
