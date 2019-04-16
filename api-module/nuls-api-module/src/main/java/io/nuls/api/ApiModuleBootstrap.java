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

import io.nuls.api.db.mongo.MongoDBTableServiceImpl;
import io.nuls.api.manager.ScheduleManager;
import io.nuls.api.model.po.config.ApiConfig;
import io.nuls.api.model.po.db.ChainInfo;
import io.nuls.api.provider.Provider;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.rpc.jsonRpc.JsonRpcServer;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.rpc.util.TimeUtils;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.config.ConfigurationLoader;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;

import java.util.List;

/**
 * api-module模块启动类
 * nuls's api module startup class
 *
 * @author captain
 * @version 1.0
 * @date 19-1-25 上午10:48
 */
@Component
public class ApiModuleBootstrap extends RpcModule {

    @Autowired
    private ApiConfig apiConfig;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":8887/ws"};
        }
        Thread.currentThread().setName("api-module-main");

        ConfigurationLoader configurationLoader = new ConfigurationLoader();
        configurationLoader.load();
        Provider.ProviderType providerType = Provider.ProviderType.valueOf(configurationLoader.getValue("providerType"));
        int defaultChainId = Integer.parseInt(configurationLoader.getValue("defaultChainId"));
        ServiceManager.init(defaultChainId, providerType);
        NulsRpcModuleBootstrap.run("io.nuls", args);
    }

    /**
     * 初始化模块相关配置
     * 有关mongoDB的连接初始化见：MongoDBService.afterPropertiesSet();
     */
    private void initCfg() {
        ApiContext.databaseUrl = apiConfig.getDatabaseUrl();
        ApiContext.databasePort = apiConfig.getDatabasePort();
        ApiContext.defaultChainId = apiConfig.getDefaultChainId();
        ApiContext.defaultAssetId = apiConfig.getDefaultAssetId();
        ApiContext.listenerIp = apiConfig.getListenerIp();
        ApiContext.rpcPort = apiConfig.getRpcPort();
    }

    /**
     * Initialize the database connection
     * 初始化数据库连接
     */
    private void initDB() {
        MongoDBTableServiceImpl tableService = SpringLiteContext.getBean(MongoDBTableServiceImpl.class);
        List<ChainInfo> chainList = tableService.getChainList();
        if (chainList == null) {
            tableService.addDefaultChain();
        } else {
            tableService.initCache();
        }
    }

    @Override
    public Module[] getDependencies() {
        return new Module[]{
                new Module(ModuleE.CS.abbr, "1.0"),
                new Module(ModuleE.BL.abbr, "1.0")
//                new Module(ModuleE.SC.abbr, "1.0")
        };
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.AP.abbr, "1.0");
    }

    @Override
    public void init() {
        try {
            super.init();
            //初始化配置项
            initCfg();

        } catch (Exception e) {
            Log.error(e);
        }
    }

    @Override
    public boolean doStart() {
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        try {
            initDB();
            ScheduleManager scheduleManager = SpringLiteContext.getBean(ScheduleManager.class);
            scheduleManager.start();
            Thread.sleep(3000);
            JsonRpcServer server = new JsonRpcServer();
            server.startServer(ApiContext.listenerIp, ApiContext.rpcPort);
            TimeUtils.getInstance().start();
            //   TimeUtils.getInstance().start(10 * 60 * 1000L);
        } catch (Exception e) {
            Log.error("------------------------api-module running failed---------------------------");
            Log.error(e);
            System.exit(-1);
        }
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }
}
