package io.nuls.cmd.client;

import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.account.AccountService;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.I18nUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-05 15:18
 * @Description: 功能描述
 */
@Component
@Slf4j
public class CmdClientModule extends RpcModule {


    @Autowired CommandHandler commandHandler;

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
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        log.info("cmd client running");
        commandHandler.start();
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }

    @Override
    public void init() {
        super.init();
//        String language = NulsConfig.MODULES_CONFIG.getCfgValue(AccountConstant.CFG_SYSTEM_SECTION, AccountConstant.CFG_SYSTEM_LANGUAGE);
        try {
            String language = "zh-CHS";
            I18nUtils.loadLanguage(this.getClass(), "languages", language);
            I18nUtils.setLanguage(language);
        } catch (NulsException e) {
            log.error("module init I18nUtils fail");
            System.exit(0);
        }

    }
}
