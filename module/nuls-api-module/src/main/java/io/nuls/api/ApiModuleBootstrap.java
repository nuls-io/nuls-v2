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
import io.nuls.api.rpc.jsonRpc.JsonRpcServer;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.config.ConfigurationLoader;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.rpc.modulebootstrap.RpcModule;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;

import java.util.List;

import static io.nuls.api.constant.ApiConstant.DEFAULT_SCAN_PACKAGE;

/**
 * api-module模块启动类
 * nuls's api module startup class
 *
 * @author vivi
 * @version 1.0
 * @date 19-2-25 上午10:48
 */
@Component
public class ApiModuleBootstrap extends RpcModule {

    @Autowired
    private ApiConfig apiConfig;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        Thread.currentThread().setName("api-module-main");

        ConfigurationLoader configurationLoader = new ConfigurationLoader();
        configurationLoader.load();
        Provider.ProviderType providerType = Provider.ProviderType.valueOf(configurationLoader.getValue("providerType"));
        int defaultChainId = Integer.parseInt(configurationLoader.getValue("chainId"));
        ServiceManager.init(defaultChainId, providerType);
        NulsRpcModuleBootstrap.run(DEFAULT_SCAN_PACKAGE, args);
    }

    @Override
    public Module[] declareDependent() {
        return new Module[]{
                new Module(ModuleE.CS.abbr, ROLE),
                new Module(ModuleE.BL.abbr, ROLE),
                new Module(ModuleE.AC.abbr, ROLE),
                new Module(ModuleE.TX.abbr, ROLE),
                new Module(ModuleE.LG.abbr, ROLE)
        };
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.AP.abbr, ROLE);
    }

    @Override
    public void init() {
        try {
            super.init();
            //初始化配置项
            initCfg();
//            LoggerUtil.init(ApiContext.defaultChainId, ApiContext.logLevel);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        }
    }

    /**
     * 初始化模块相关配置
     * 有关mongoDB的连接初始化见：MongoDBService.afterPropertiesSet();
     */
    private void initCfg() {
        ApiContext.databaseUrl = apiConfig.getDatabaseUrl();
        ApiContext.databasePort = apiConfig.getDatabasePort();
        ApiContext.defaultChainId = apiConfig.getChainId();
        ApiContext.defaultAssetId = apiConfig.getAssetId();
        //ApiContext.defaultChainName = apiConfig.getChainName();
        ApiContext.defaultChainName = "nuls";
        ApiContext.defaultSymbol = apiConfig.getSymbol();
        ApiContext.listenerIp = apiConfig.getListenerIp();
        ApiContext.rpcPort = apiConfig.getRpcPort();
        ApiContext.logLevel = apiConfig.getLogLevel();
        ApiContext.maxWaitTime = apiConfig.getMaxWaitTime();
        ApiContext.maxAliveConnect = apiConfig.getMaxAliveConnect();
        ApiContext.connectTimeOut = apiConfig.getConnectTimeOut();
    }

    @Override
    public boolean doStart() {
        initDB();
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        try {
            if (hasDependent(ModuleE.SC)) {
                ApiContext.isRunSmartContract = true;
            }
            if (hasDependent(ModuleE.CC)) {
                ApiContext.isRunCrossChain = true;
            }

            ScheduleManager scheduleManager = SpringLiteContext.getBean(ScheduleManager.class);
            JsonRpcServer server = new JsonRpcServer();
            server.startServer(ApiContext.listenerIp, ApiContext.rpcPort);

            Thread.sleep(3000);
            scheduleManager.start();
        } catch (Exception e) {
            LoggerUtil.commonLog.error("------------------------api-module running failed---------------------------");
            LoggerUtil.commonLog.error(e);
            System.exit(-1);
        }
        return RpcModuleState.Running;
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
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }
}
