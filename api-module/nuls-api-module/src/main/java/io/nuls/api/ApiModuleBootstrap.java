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

package io.nuls.api;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import io.nuls.api.constant.RunningStatusEnum;
import io.nuls.api.db.DBTableService;
import io.nuls.api.db.MongoDBService;
import io.nuls.api.manager.ScheduleManager;
import io.nuls.api.model.po.db.ChainInfo;
import io.nuls.api.rpc.jsonRpc.JsonRpcServer;
import io.nuls.api.utils.ConfigLoader;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.netty.bootstrap.NettyServer;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.core.ioc.SpringLiteContext;

import java.util.List;

import static io.nuls.api.constant.ApiConstant.DEFAULT_SCAN_PACKAGE;
import static io.nuls.api.constant.ApiConstant.RPC_DEFAULT_SCAN_PACKAGE;
import static io.nuls.api.utils.LoggerUtil.commonLog;

/**
 * api-module模块启动类
 * nuls's api module startup class
 *
 * @author captain
 * @version 1.0
 * @date 19-1-25 上午10:48
 */
public class ApiModuleBootstrap {

    public static void main(String[] args) {
        Thread.currentThread().setName("api-module-main");
        try {
            init();
            start();
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error("error occur when init, " + e.getMessage());
        }
//        loop();
    }

    /**
     * 初始化，完成后系统状态变更为{@link RunningStatusEnum#READY}
     */
    private static void init() throws Exception {
        //加载配置
        ConfigLoader.load();
        //扫描包路径io.nuls.api,初始化bean
        springInit();
        //初始化数据库相关
        initDB();
        //初始化远程连接
        initServer();
    }


    private static void springInit() {
        String dbName = "nuls-api";
        MongoClient mongoClient = new MongoClient(ApiContext.dbIp, ApiContext.port);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(dbName);
        MongoDBService mongoDBService = new MongoDBService(mongoClient, mongoDatabase);
        SpringLiteContext.putBean("dbService", mongoDBService);

        SpringLiteContext.init(DEFAULT_SCAN_PACKAGE);
    }

    /**
     * Initialize the database connection
     * 初始化数据库连接
     */
    private static void initDB() {
        DBTableService tableService = SpringLiteContext.getBean(DBTableService.class);
        List<ChainInfo> chainList = tableService.getChainList();
        if (chainList == null) {
            tableService.addDefaultChain();
        } else {
            tableService.initCache();
        }
    }

    private static void initServer() {
        try {
            //rpc服务初始化
            NettyServer.getInstance(ModuleE.AP)
                    .moduleRoles(new String[]{"1.0"})
                    .moduleVersion("1.0")
                    .dependencies(ModuleE.KE.abbr, "1.0")
//                    .dependencies(ModuleE.CM.abbr, "1.0")
//                    .dependencies(ModuleE.AC.abbr, "1.0")
//                    .dependencies(ModuleE.NW.abbr, "1.0")
//                    .dependencies(ModuleE.CS.abbr, "1.0")
                    .dependencies(ModuleE.BL.abbr, "1.0")
//                    .dependencies(ModuleE.LG.abbr, "1.0")
//                    .dependencies(ModuleE.TX.abbr, "1.0")
//                    .dependencies(ModuleE.PU.abbr, "1.0")
                    .scanPackage(RPC_DEFAULT_SCAN_PACKAGE);
            // Get information from kernel
            String kernelUrl = "ws://" + HostInfo.getLocalIP() + ":8887/ws";
            ConnectManager.getConnectByUrl(kernelUrl);
            ResponseMessageProcessor.syncKernel(kernelUrl);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error("error occur when init, " + e.getMessage());
        }
    }

    /**
     * 启动，完成后系统状态变更为{@link RunningStatusEnum#RUNNING}
     */
    private static void start() {
//        ScheduleManager scheduleManager = SpringLiteContext.getBean(ScheduleManager.class);
//        scheduleManager.start();

        String ip = "0.0.0.0";
        int port = 8080;

        JsonRpcServer server = new JsonRpcServer();

        server.startServer(ip, port);
    }

    /**
     * 循环记录一些日志信息
     */
    private static void loop() {

    }

}
