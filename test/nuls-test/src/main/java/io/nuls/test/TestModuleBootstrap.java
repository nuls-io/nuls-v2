package io.nuls.test;

import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.core.config.ConfigurationLoader;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 14:33
 * @Description: 功能描述
 */
public class TestModuleBootstrap {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771","1"};
        }else{
            args = new String[]{args[0],"1"};
        }
        ConfigurationLoader configurationLoader = new ConfigurationLoader();
        configurationLoader.load();
        Provider.ProviderType providerType = Provider.ProviderType.valueOf(configurationLoader.getValue("providerType"));
        int defaultChainId = Integer.parseInt(configurationLoader.getValue("chainId"));
        ServiceManager.init(defaultChainId,providerType);
        NulsRpcModuleBootstrap.run("io.nuls.test",args);
    }


}
