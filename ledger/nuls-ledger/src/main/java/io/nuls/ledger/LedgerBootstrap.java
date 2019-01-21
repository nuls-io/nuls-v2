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
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
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
            initServer();
        } catch (Exception e) {
            logger.error("ledger Bootstrap failed", e);
            System.exit(-1);
        }
    }


    /**
     * 初始化websocket服务器，供其他模块调用本模块接口
     *
     * @throws Exception
     */
    public static void initServer() {
        try {
            String packageC = "io.nuls.ledger.rpc.cmd";
            String kernelUrl = ModuleConfig.getInstance().getKernelHost() + ":" + ModuleConfig.getInstance().getKernelPort();
            logger.info("kernel start info {}", kernelUrl);
            WsServer.getInstance(ModuleE.LG)
                    //.supportedAPIVersions(new String[]{"1.1", "1.2"})
                    .moduleRoles(ModuleE.LG.abbr, new String[]{"1.1", "1.2"})
                    .moduleVersion("1.2")
                    .scanPackage(packageC)
                    .connect("ws://127.0.0.1:8887");
            CmdDispatcher.syncKernel();
        } catch (Exception e) {
            logger.error("ledger initServer failed", e);
        }
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
