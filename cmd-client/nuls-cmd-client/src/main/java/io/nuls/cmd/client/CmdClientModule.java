package io.nuls.cmd.client;

import io.nuls.api.provider.Provider;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.account.AccountService;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.annotation.Value;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.model.StringUtils;
import io.nuls.tools.parse.I18nUtils;
import io.nuls.tools.thread.ThreadUtils;
import jline.console.ConsoleReader;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-05 15:18
 * @Description: 功能描述
 */
@Component
@Slf4j
public class CmdClientModule extends RpcModule {

    @Autowired Config config;

//    ServerSocket serverSocket;
//
//    int port = 1122;

    @Value("chain-id")
    int defaultChainId;

    @Value("provider-type")
    Provider.ProviderType providerType;

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
        log.info("chainId:{}",config.getChainId());
        config.setChainId(12345);
        commandHandler.start();
//        ThreadUtils.createAndRunThread("socket",()->{
//            while(true){
//                Socket socket = null;
//                try {
//                    socket = serverSocket.accept();
//                    System.out.println("New connection accepted "+
//                            socket.getInetAddress()+":"+socket.getPort());
//                    commandHandler.start();
////                    ConsoleReader reader = new ConsoleReader(socket.getInputStream(),socket.getOutputStream());
////                    String line;
////                    do {
////                        line = reader.readLine(CommandConstant.COMMAND_PS1);
////                        if (StringUtils.isBlank(line) && "nuls>>>".equals(line)) {
////                            continue;
////                        }
////                        System.out.print(line + "====>\n");
////                    } while (line != null);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }finally{
//                    if(socket!=null){
//                        try {
//                            socket.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        });

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
            ServiceManager.init(defaultChainId,providerType);
//            serverSocket = new ServerSocket(port,1);
//            System.out.println("服务器启动!");
        } catch (Exception e) {
            log.error("module init I18nUtils fail");
            System.exit(0);
        }

    }
}
