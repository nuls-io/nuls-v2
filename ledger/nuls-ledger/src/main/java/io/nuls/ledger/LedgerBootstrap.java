/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger;

import io.nuls.db.service.RocksDBService;
import io.nuls.ledger.config.AppConfig;
import io.nuls.ledger.model.ModuleConfig;
import io.nuls.ledger.service.BlockDataService;
import io.nuls.ledger.service.impl.BlockDataServiceImpl;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.netty.bootstrap.NettyServer;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * @author: Niels Wang
 * @date: 2018/10/15
 */
public class LedgerBootstrap {

    public static void main(String[] args) {
        logger.info("ledger Bootstrap start...");
        try {
            AppConfig.loadModuleConfig();
            initRocksDb();
            //springLite容器初始化AppInitializing
            SpringLiteContext.init("io.nuls.ledger", new ModularServiceMethodInterceptor());
            initLedgerDatas();
            initRpcServer();
        } catch (Exception e) {
            logger.error("ledger Bootstrap failed", e);
            System.exit(-1);
        }
    }


    /**
     * 共识模块启动WebSocket服务，用于其他模块连接共识模块与账本模块RPC交互
     */
    private static void initRpcServer() throws Exception {
            String packageC = "io.nuls.ledger.rpc.cmd";
            NettyServer.getInstance(ModuleE.LG)
                    .moduleRoles(new String[]{"1.0"})
                    .moduleVersion("1.0")
                    .dependencies(ModuleE.KE.abbr, "1.1")
                    .scanPackage(packageC);
            String kernelUrl = "ws://" + HostInfo.getLocalIP() + ":8887/ws";
            /*
             * 链接到指定地址
             * */
            ConnectManager.getConnectByUrl(kernelUrl);
            /*
             * 和指定地址同步
             * */
            ResponseMessageProcessor.syncKernel(kernelUrl);

    }

    /**
     * 进行数据的校验处理,比如异常关闭模块造成的数据不一致。
     * 确认的高度是x,则进行x高度的数据恢复处理
     */
    public static void initLedgerDatas() throws Exception {
        BlockDataService blockDataService = SpringLiteContext.getBean(BlockDataServiceImpl.class);
        blockDataService.initBlockDatas();
    }

    /**
     * 初始化数据库
     */
    public static void initRocksDb() {
        try {
            RocksDBService.init(ModuleConfig.getInstance().getDatabaseDir());

        } catch (Exception e) {
            logger.error(e);
        }
    }
}
