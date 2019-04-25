package io.nuls.rpc.util;

import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.RpcModule;

import java.util.Set;

/**
 * 辅助类，判断节点是够支持一些额外的功能
 *
 * @author captain
 * @version 1.0
 * @date 2019/4/25 16:44
 */
public class ModuleHelper {

    private static boolean supportProtocolUpdate;
    private static boolean supportSmartContract;
    private static boolean supportCrossChain;

    public static boolean isSupportProtocolUpdate() {
        return supportProtocolUpdate;
    }

    public static boolean isSupportSmartContract() {
        return supportSmartContract;
    }

    public static boolean isSupportCrossChain() {
        return supportCrossChain;
    }

    public static void init(RpcModule module) {
        supportProtocolUpdate = enableProtocolUpdate(module);
        supportSmartContract = enableSmartContract(module);
        supportCrossChain = enableCrossChain(module);
    }

    /**
     * 是否支持协议升级功能
     *
     * @param module
     * @return
     */
    private static boolean enableProtocolUpdate(RpcModule module) {
        Module m = new Module(ModuleE.PU.abbr, "1.0");
        Set<Module> dependencies = module.getDependencies();
        return module.moduleInfo().equals(m) || dependencies.contains(m);
    }

    /**
     * 是否支持智能合约功能
     *
     * @param module
     * @return
     */
    private static boolean enableSmartContract(RpcModule module) {
        Module m = new Module(ModuleE.SC.abbr, "1.0");
        Set<Module> dependencies = module.getDependencies();
        return module.moduleInfo().equals(m) || dependencies.contains(m);
    }

    /**
     * 是否支持跨链功能
     *
     * @param module
     * @return
     */
    private static boolean enableCrossChain(RpcModule module) {
        Module m = new Module(ModuleE.CC.abbr, "1.0");
        Set<Module> dependencies = module.getDependencies();
        return module.moduleInfo().equals(m) || dependencies.contains(m);
    }

}
