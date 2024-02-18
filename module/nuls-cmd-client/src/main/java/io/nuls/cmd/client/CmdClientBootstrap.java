package io.nuls.cmd.client;

import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.cmd.client.utils.AssetsUtil;
import io.nuls.core.core.config.ConfigurationLoader;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.rpc.util.AddressPrefixDatas;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 17:07
 * @Description: Function Description
 */
public class CmdClientBootstrap {
    public static void main(String[] args) {
        NulsRpcModuleBootstrap.printLogo("/cli-logo");
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        ConfigurationLoader configurationLoader = new ConfigurationLoader();
        configurationLoader.load();
        Provider.ProviderType providerType = Provider.ProviderType.valueOf(configurationLoader.getValue("providerType"));
        int defaultChainId = Integer.parseInt(configurationLoader.getValue("chainId"));
        ServiceManager.init(defaultChainId,providerType);
        try {
            NulsRpcModuleBootstrap.run("io.nuls.cmd.client",args);
            //Add address tool class initialization
            AddressTool.init(new AddressPrefixDatas());

        }catch (Exception e){
            Log.error("module start fail {}",e.getMessage());
        }
    }
}
