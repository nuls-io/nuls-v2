package io.nuls.eventbus.init;



import io.nuls.eventbus.EventBus;
import io.nuls.eventbus.constant.EbConstants;
import io.nuls.eventbus.model.Topic;
import io.nuls.eventbus.rpc.processor.ClientSyncProcessor;
import io.nuls.eventbus.service.EbStorageService;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.I18nUtils;
import io.nuls.tools.parse.config.IniEntity;
import io.nuls.tools.thread.TimeService;
import static io.nuls.eventbus.util.EbLog.Log;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Bootstrap class for Event Bus module.
 * <p> It does following operations
 *  <ul>
 *      <li>Loads module configuration data from modules.ini file</li>
 *      <li>Initializes annotated driven dependency management</li>
 *      <li>Starts {@code TimeService} thread</li>
 *      <li>initializes Rocks DB and loads topics from the db</li>
 *      <li>starts websocket server and registers Event Bus module with Kernel module</li>
 *      <li>starts {@code ClientSyncProcessor} thread to sync role connection info</li>
 *  </ul>
 * </p>
 * @author naveen
 */
public class Bootstrap {

    /**
     *  main() method for starting the module
     * @param args
     */
    public static void main(String[] args){
        Log.info("Event Bus module modulebootstrap starts...");
        try {
            initConfig();
            SpringLiteContext.init(EbConstants.EB_BASE_PACKAGE,new ModularServiceMethodInterceptor());
            TimeService.getInstance().start();
            initDB();
            startRpc();
            syncClientConnectionInfo();
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

    private static void syncClientConnectionInfo(){
        Set<String> roles = EventBus.getInstance().getAllSubscribers();
        if(roles != null){
            for(String role : roles){
                EbConstants.CLIENT_SYNC_POOL.submit(new ClientSyncProcessor(new Object[]{role, EbConstants.SUBSCRIBE}));
            }
        }
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
