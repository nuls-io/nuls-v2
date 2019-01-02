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
import io.nuls.ledger.db.DataBaseArea;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Niels Wang
 * @date: 2018/10/15
 */
public class LedgerBootstrap {

    static final Logger logger = LoggerFactory.getLogger(LedgerBootstrap.class);


    public static void main(String[] args) {
        Log.info("ledger Bootstrap start...");
        try {
            AppConfig.loadModuleConfig();
            initRocksDb();
            initServer();
            //springLite容器初始化AppInitializing
            SpringLiteContext.init("io.nuls.ledger", new ModularServiceMethodInterceptor());

        } catch (Exception e) {
            Log.error("ledger Bootstrap failed", e);
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
            String kernelUrl = AppConfig.loadModuleConfig().getKernelHost() + ":" + AppConfig.loadModuleConfig().getKernelPort();
            logger.info("kernel start info {}", kernelUrl);
            WsServer.getInstance(ModuleE.LG)
                    //.supportedAPIVersions(new String[]{"1.1", "1.2"})
                    .moduleRoles(ModuleE.LG.abbr, new String[]{"1.1", "1.2"})
                    .moduleVersion("1.2")
                    .scanPackage(packageC)
                    .connect("ws://127.0.0.1:8887");

            CmdDispatcher.syncKernel();
        } catch (Exception e) {
            Log.error("ledger initServer failed", e);
        }
    }


    /**
     * 初始化数据库
     */
    public static void initRocksDb() {
        try {
            RocksDBService.init(AppConfig.loadModuleConfig().getDatabaseDir());
            if (!RocksDBService.existTable(DataBaseArea.TB_LEDGER_ACCOUNT)) {
                RocksDBService.createTable(DataBaseArea.TB_LEDGER_ACCOUNT);
            } else {
                Log.info("table {} exist.", DataBaseArea.TB_LEDGER_ACCOUNT);
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
