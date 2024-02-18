package io.nuls.base.protocol;

import io.nuls.core.log.Log;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.RpcModule;

import java.util.Set;

/**
 * Auxiliary class, determining whether the node is capable of supporting some additional functions
 *
 * @author captain
 * @version 1.0
 * @date 2019/4/25 16:44
 */
public class ModuleHelper {

    /**
     * Does it support protocol upgrade functionality
     */
    private static boolean supportProtocolUpdate;
    /**
     * Does it support smart contract functionality
     */
    private static boolean supportSmartContract;
    /**
     * Does it support cross chain functionality
     */
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
        supportProtocolUpdate = true;
        supportSmartContract = true;
        supportCrossChain = true;
        Log.info("----------------ModuleHelper----------------");
        Log.info("module-" + module.toString());
        Log.info("supportProtocolUpdate-"+supportProtocolUpdate);
        Log.info("supportSmartContract-"+supportSmartContract);
        Log.info("supportCrossChain-"+supportCrossChain);
    }

    private static boolean enableProtocolUpdate(RpcModule module) {
        Module m = new Module(ModuleE.PU.abbr, "1.0");
        Set<Module> dependencies = module.getDependencies();
        return module.moduleInfo().equals(m) || dependencies.contains(m);
    }

    private static boolean enableSmartContract(RpcModule module) {
        Module m = new Module(ModuleE.SC.abbr, "1.0");
        Set<Module> dependencies = module.getDependencies();
        return module.moduleInfo().equals(m) || dependencies.contains(m);
    }

    private static boolean enableCrossChain(RpcModule module) {
        Module m = new Module(ModuleE.CC.abbr, "1.0");
        Set<Module> dependencies = module.getDependencies();
        return module.moduleInfo().equals(m) || dependencies.contains(m);
    }

}
