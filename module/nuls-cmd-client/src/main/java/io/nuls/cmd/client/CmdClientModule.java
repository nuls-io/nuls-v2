package io.nuls.cmd.client;

import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.system.EvalProcessor;
import io.nuls.cmd.client.utils.AssetsUtil;
import io.nuls.cmd.client.utils.LoggerUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.parse.I18nUtils;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.RpcModule;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.core.thread.ThreadUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-05 15:18
 * @Description: 功能描述
 */
@Component
public class CmdClientModule extends RpcModule {

    int waiting = 1;

    @Autowired
    Config config;

    @Autowired
    CommandHandler commandHandler;

    static NulsLogger log = LoggerUtil.logger;

    @Override
    public Module[] declareDependent() {
        return new Module[]{
                //new Module(ModuleE.NW.abbr, ROLE),
                //new Module(ModuleE.AC.abbr, ROLE),
                //new Module(ModuleE.TX.abbr, ROLE),
                //new Module(ModuleE.BL.abbr, ROLE),
                //new Module(ModuleE.CS.abbr, ROLE),
                //new Module(ModuleE.LG.abbr, ROLE)
                new Module(ModuleE.NC.abbr, ROLE)
        };
    }

    @Autowired
    EvalProcessor evalProcessor;

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.Constant.CMD, ROLE);
    }

    @Override
    public boolean doStart() {
        System.out.println("waiting nuls-wallet base module ready");
        ThreadUtils.createAndRunThread("", () -> {
            while (true) {
                if (this.isDependencieReady()) {
                    return;
                }
                waiting++;
                System.out.print(" " + waiting);
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (waiting > 59) {
                    Log.error("waiting nuls-wallet base module ready timeout ");
                    System.exit(0);
                }
            }
        });
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        System.out.println("nuls-wallet base module ready");

        Arrays.stream(this.startArgs).forEach(d->{
            Log.info("arg:{}",d);
        });
        if(startArgs.length > 1){
            String evel = startArgs[1];
            if(evel.equals(evalProcessor.getCommand())){
                if(startArgs.length < 2){
                    System.out.println("param is error");
                }
                String[] cmdAry = startArgs[2].split(",");
                Arrays.stream(cmdAry).forEach(cmd->{
                    try {
                        commandHandler.processCommand(cmd);
                    } catch (UnsupportedEncodingException e) {
                        System.out.println(CommandConstant.EXCEPTION + ": " + e.getMessage());
                    }
                });
                System.exit(0);
            }
        }
        ThreadUtils.createAndRunThread("cmd", () -> commandHandler.start());
        if(this.hasDependent(ModuleE.CC)){
            //增加跨链资产信息获取
            AssetsUtil.initRegisteredChainInfo();
        }

        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }

    @Override
    public void init() {
        super.init();
        try {
            I18nUtils.setLanguage(config.getLanguage());
        } catch (Exception e) {
            log.error("module init I18nUtils fail", e);
            System.exit(0);
        }

    }
}
