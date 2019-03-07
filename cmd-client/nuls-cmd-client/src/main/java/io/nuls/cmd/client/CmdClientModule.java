package io.nuls.cmd.client;

import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.account.AccountService;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.tools.core.annotation.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-05 15:18
 * @Description: 功能描述
 */
@Component
@Slf4j
public class CmdClientModule extends RpcModule {

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Override
    public Module[] getDependencies() {
        return new Module[]{
                new Module(ModuleE.AC.abbr,"1.0")
        };
    }

    @Override
    public Module moduleInfo() {
        return new Module("cmd-client","1.0");
    }

    @Override
    public boolean doStart() {
        log.info("cmd client start");
        log.info(accountService.hello());
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        log.info("cmd client running");
        CommandHandler.main();
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }

}
