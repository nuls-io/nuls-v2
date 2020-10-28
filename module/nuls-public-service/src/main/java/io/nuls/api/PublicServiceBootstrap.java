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

import com.fasterxml.jackson.core.JsonParser;
import com.google.common.cache.Cache;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.constant.config.ApiConfig;
import io.nuls.api.db.mongo.MongoChainServiceImpl;
import io.nuls.api.db.mongo.MongoDBTableServiceImpl;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.manager.ScheduleManager;
import io.nuls.api.model.po.AssetInfo;
import io.nuls.api.model.po.ChainInfo;
import io.nuls.api.model.po.SyncInfo;
import io.nuls.api.rpc.jsonRpc.JsonRpcServer;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.config.ConfigurationLoader;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.rpc.modulebootstrap.RpcModule;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.core.rpc.util.AddressPrefixDatas;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static io.nuls.api.constant.ApiConstant.DEFAULT_SCAN_PACKAGE;

/**
 * public-service模块启动类
 * nuls's public-service startup class
 *
 * @author vivi
 * @version 1.0
 * @date 19-2-25 上午10:48
 */
@Component
public class PublicServiceBootstrap extends RpcModule {

    @Autowired
    private ApiConfig apiConfig;
    @Autowired
    private AddressPrefixDatas addressPrefixDatas;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        Thread.currentThread().setName("public-service-main");

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
                new Module(ModuleE.LG.abbr, ROLE),
                new Module(ModuleE.NW.abbr, ROLE)
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
            /**
             * 地址工具初始化
             */
            AddressTool.init(addressPrefixDatas);
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
        ApiContext.mainChainId = apiConfig.getMainChainId();
        ApiContext.mainAssetId = apiConfig.getMainAssetId();
        ApiContext.mainSymbol = apiConfig.getMainSymbol();
        ApiContext.defaultChainId = apiConfig.getChainId();
        ApiContext.defaultAssetId = apiConfig.getAssetId();
        ApiContext.defaultSymbol = apiConfig.getSymbol();
        ApiContext.defaultChainName = apiConfig.getChainName();
        ApiContext.defaultDecimals = apiConfig.getDecimals();

        ApiContext.databaseUrl = apiConfig.getDatabaseUrl();
        ApiContext.databasePort = apiConfig.getDatabasePort();
        ApiContext.listenerIp = apiConfig.getListenerIp();
        ApiContext.rpcPort = apiConfig.getRpcPort();
        ApiContext.logLevel = apiConfig.getLogLevel();
        ApiContext.maxWaitTime = apiConfig.getMaxWaitTime();
        ApiContext.maxAliveConnect = apiConfig.getMaxAliveConnect();
        ApiContext.connectTimeOut = apiConfig.getConnectTimeOut();
        ApiContext.socketTimeout = apiConfig.getSocketTimeout();
        ApiContext.syncCoinBase = apiConfig.isSyncCoinBase();
        if (StringUtils.isNotBlank(apiConfig.getSyncAddress())) {
            for (String address : apiConfig.getSyncAddress().split(",")) {
                ApiContext.syncAddress.add(address);
            }
        }

        ApiContext.blackHolePublicKey = Hex.decode(apiConfig.getBlackHolePublicKey());
        if (apiConfig.getDeveloperNodeAddress() != null) {
            ApiContext.DEVELOPER_NODE_ADDRESS = new HashSet(Arrays.asList(apiConfig.getDeveloperNodeAddress().split(",")));
        }
        if (apiConfig.getAmbassadorNodeAddress() != null) {
            ApiContext.AMBASSADOR_NODE_ADDRESS = new HashSet(Arrays.asList(apiConfig.getAmbassadorNodeAddress().split(",")));
        }
        if (apiConfig.getMappingAddress() != null) {
            ApiContext.MAPPING_ADDRESS = new HashSet(Arrays.asList(apiConfig.getMappingAddress().split(",")));
        }
        ApiContext.BUSINESS_ADDRESS = apiConfig.getBusinessAddress();
        ApiContext.TEAM_ADDRESS = apiConfig.getTeamAddress();
        ApiContext.COMMUNITY_ADDRESS = apiConfig.getCommunityAddress();
        JSONUtils.getInstance().configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

    }

    @Override
    public boolean doStart() {
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        try {
            Result<Map> result = WalletRpcHandler.getConsensusConfig(ApiContext.defaultChainId);
            if (result.isSuccess()) {
                Map<String, Object> configMap = result.getData();
                ApiContext.agentChainId = (int) configMap.get("agentChainId");
                ApiContext.agentAssetId = (int) configMap.get("agentAssetId");
                ApiContext.awardAssetId = (int) configMap.get("awardAssetId");
                ApiContext.minDeposit = new BigInteger(configMap.get("commissionMin").toString());
            }
            initDB();

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
            LoggerUtil.commonLog.error("------------------------public-service running failed---------------------------");
            LoggerUtil.commonLog.error(e);
            System.exit(-1);
        }
        ApiContext.isReady = true;
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
            tableService.addDefaultChainCache();
        } else {
            tableService.initCache();
        }

        MongoChainServiceImpl chainService = SpringLiteContext.getBean(MongoChainServiceImpl.class);
        SyncInfo syncInfo = chainService.getSyncInfo(ApiContext.defaultChainId);
        if (syncInfo != null) {
            ApiContext.protocolVersion = syncInfo.getVersion();
        }

        List<ChainInfo> chainInfoList = chainService.getChainInfoList();
        if (chainInfoList != null) {
            for (ChainInfo chainInfo : chainInfoList) {
                CacheManager.getChainInfoMap().put(chainInfo.getChainId(), chainInfo);
                for (AssetInfo assetInfo : chainInfo.getAssets()) {
                    CacheManager.getAssetInfoMap().put(assetInfo.getKey(), assetInfo);
                }
            }
        }
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }

    @Override
    protected long getTryRuningTimeout() {
        return 360;
    }
}
