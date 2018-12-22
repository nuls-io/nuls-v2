package io.nuls.eventbus.init;



import io.nuls.eventbus.EventBus;
import io.nuls.eventbus.constant.EbConstants;
import io.nuls.eventbus.model.Topic;
import io.nuls.eventbus.rpc.processor.ClientSyncProcessor;
import io.nuls.eventbus.runtime.EventBusRuntime;
import io.nuls.eventbus.service.EbStorageService;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.I18nUtils;
import io.nuls.tools.parse.config.IniEntity;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author naveen
 */
public class Bootstrap {

    public static void main(String[] args){
        Log.info("Event Bus module bootstrap starts...");
        try {
            initConfig();
            SpringLiteContext.init(EbConstants.EB_BASE_PACKAGE,new ModularServiceMethodInterceptor());
            TimeService.getInstance().start();
            initDB();
            startRpc();
            // TODO register Event Bus commands
            startProcessors();
        }catch (Exception e){
            Log.error("Event Bus module Bootstrap failed..exiting the system");
            System.exit(1);
        }
    }

    private static void initConfig(){
        try {
            IniEntity moduleConfig = ConfigLoader.loadIni(EbConstants.MODULE_FILE);
            EbConstants.MODULE_CONFIG_MAP.put(EbConstants.LANGUAGE,moduleConfig.getCfgValue(EbConstants.SYSTEM_SECTION, EbConstants.LANGUAGE));
            EbConstants.MODULE_CONFIG_MAP.put(EbConstants.ENCODING,moduleConfig.getCfgValue(EbConstants.SYSTEM_SECTION, EbConstants.ENCODING));
            EbConstants.MODULE_CONFIG_MAP.put(EbConstants.KERNEL_URL,moduleConfig.getCfgValue(EbConstants.SYSTEM_SECTION, EbConstants.KERNEL_URL));
            EbConstants.MODULE_CONFIG_MAP.put(EbConstants.ROCKS_DB_PATH,moduleConfig.getCfgValue(EbConstants.DB_SECTION, EbConstants.ROCKS_DB_PATH));
            I18nUtils.loadLanguage("languages",EbConstants.MODULE_CONFIG_MAP.get(EbConstants.LANGUAGE));
            I18nUtils.setLanguage(EbConstants.MODULE_CONFIG_MAP.get(EbConstants.LANGUAGE));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startRpc() throws Exception{
        try{
            WsServer.getInstance(ModuleE.EB)
                    .moduleRoles(new String[]{"1.0"})
                    .moduleVersion("1.0")
                    .scanPackage(EbConstants.RPC_PACKAGE_EB)
                    .connect(EbConstants.MODULE_CONFIG_MAP.get(EbConstants.KERNEL_URL));
            CmdDispatcher.syncKernel();
        }catch (Exception e){
            Log.error("Event Bus rpc start up failed");
            throw new Exception("Event Bus rpc start up failed");
        }
    }

    private static void startProcessors(){
        Set<String> roles = EventBus.getInstance().getAllSubscribers();
        if(roles != null){
            roles.stream().map(role -> EventBusRuntime.CLIENT_SYNC_QUEUE.offer(new Object[]{role, EbConstants.SUBSCRIBE}));
        }
        EbConstants.EB_THREAD_POOL.execute(new ClientSyncProcessor());
    }

    private static void initDB(){
        EbStorageService ebStorageService = SpringLiteContext.getBean(EbStorageService.class);
        ebStorageService.init();
        ConcurrentMap<String,Topic> map = ebStorageService.loadTopics();
        if(!map.isEmpty()){
            EventBus.getInstance().setTopicMap(map);
        }
    }

}
