package io.nuls.mykernel;

import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.tools.core.annotation.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: zhoulijun
 * @Time: 2019-02-28 14:28
 * @Description: 功能描述
 */
@Component
@Slf4j
public class KernelModule extends RpcModule {

    @Override
    public Module[] getDependencies() {
        return new Module[]{new Module(ModuleE.AC.abbr,"1.0")};
    }

    @Override
    public String getRpcCmdPackage() {
        return "io.nuls";
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.NW.abbr,"1.0");
    }

    @Override
    public boolean doStart() {
        log.info("module readying");
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        log.info("module running");
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module module) {
        log.info("module stop");
        return RpcModuleState.Ready;
    }

    public static void main(String[] args) {
        NulsRpcModuleBootstrap.run("io.nuls.account",args);
    }

}
