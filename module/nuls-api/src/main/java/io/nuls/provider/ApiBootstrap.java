package io.nuls.provider;

import com.sun.net.httpserver.Filter;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.I18nUtils;
import io.nuls.provider.api.RpcServerManager;
import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.core.annotation.Value;
import io.nuls.core.core.config.ConfigurationLoader;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.rpc.modulebootstrap.RpcModule;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.provider.api.config.Config;
import io.nuls.provider.api.config.Context;
import io.nuls.provider.api.model.AssetInfo;
import io.nuls.provider.api.model.ChainInfo;
import io.nuls.provider.task.ScheduleManager;
import io.nuls.v2.NulsSDKBootStrap;
import org.checkerframework.checker.units.qual.A;

import java.util.Map;

import static io.nuls.provider.api.constant.SdkConstant.SDK_API;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-10 19:27
 * @Description: 模块启动类
 */
@Service
public class ApiBootstrap extends RpcModule {

    private String moduleName = "nuls-api";

    @Autowired
    MyModule myModule;
    @Autowired
    private Config config;

    public static void main(String[] args) {
        boolean isOffline = false;
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
            //args = new String[]{"ws://192.168.1.40:7771"};
        } else {
            String arg1 = args[0];
            if (StringUtils.isNotBlank(arg1)) {
                arg1 = arg1.trim().toLowerCase();
            }
            if ("offline".equals(arg1)) {
                isOffline = true;
            }
        }
        String basePackage = "io.nuls";
        ConfigurationLoader configurationLoader = new ConfigurationLoader();
        configurationLoader.load();
        int defaultChainId = Integer.parseInt(configurationLoader.getValue("chainId"));
        Provider.ProviderType providerType = Provider.ProviderType.RPC;
        ServiceManager.init(defaultChainId, providerType);
        Map<String, ConfigurationLoader.ConfigItem> configItemMap = configurationLoader.getConfigData().get(SDK_API);
        ConfigurationLoader.ConfigItem offline = configItemMap.get("offline");
        if (offline != null) {
            isOffline = Boolean.parseBoolean(offline.getValue());
        }
        if (!isOffline) {
            NulsRpcModuleBootstrap.run("io.nuls", args);
        } else {
            SpringLiteContext.init(basePackage);
        }
        initRpcServer(configItemMap);
        NulsSDKBootStrap.init(defaultChainId, "");
        try {
            I18nUtils.setLanguage("en");
        } catch (NulsException e) {
            e.printStackTrace();
        }
    }

    private static void initRpcServer(Map<String, ConfigurationLoader.ConfigItem> configItemMap) {
        String server_ip = "0.0.0.0";
        int server_port = 18004;
        if (configItemMap != null) {
            ConfigurationLoader.ConfigItem serverIp = configItemMap.get("serverIp");
            if (serverIp != null) {
                server_ip = serverIp.getValue();
            }
            ConfigurationLoader.ConfigItem serverPort = configItemMap.get("serverPort");
            if (serverPort != null) {
                server_port = Integer.parseInt(serverPort.getValue());
            }
        }
        RpcServerManager.getInstance().startServer(server_ip, server_port);
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
        return new Module(moduleName, ROLE);
    }

    @Override
    public boolean doStart() {
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        if (hasDependent(ModuleE.CC)) {
            Context.isRunCrossChain = true;
        }

        ChainInfo chainInfo = new ChainInfo();
        chainInfo.setChainId(config.getChainId());
        chainInfo.setChainName(config.getChainName());

        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setChainId(config.getChainId());
        assetInfo.setAssetId(config.getAssetsId());
        assetInfo.setSymbol(config.getSymbol());
        assetInfo.setDecimals(config.getDecimals());

        chainInfo.getAssets().add(assetInfo);
        Context.defaultChain = chainInfo;

        ScheduleManager scheduleManager = SpringLiteContext.getBean(ScheduleManager.class);
        scheduleManager.start();
        return myModule.startModule(moduleName);
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Running;
    }

}
