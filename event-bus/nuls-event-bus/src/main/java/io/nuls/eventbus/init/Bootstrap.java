package io.nuls.eventbus.init;



import io.nuls.eventbus.EventBus;
import io.nuls.eventbus.model.Topic;
import io.nuls.eventbus.rpc.processor.ClientSyncProcessor;
import io.nuls.eventbus.rpc.processor.EventBusRuntime;
import io.nuls.eventbus.rpc.processor.EventDispatchProcessor;
import io.nuls.eventbus.service.EbStorageService;
import io.nuls.eventbus.service.EbStorageServiceImpl;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author naveen
 */
public class Bootstrap {

    private static EbStorageService ebStorageService = new EbStorageServiceImpl();

    public static void main(String args[]){
        Log.info("Event Bus module bootstrap starts...");
        try {
            init();
            initDB();
            startRpc();
            // TODO register Event Bus commands
            TimeService.getInstance().start();
            startProcessors();
        }catch (Exception e){
            Log.error("Event Bus module Bootstrap failed..exiting the system");
            System.exit(1);
        }
    }

    public static void init(){
        // TODO initialize module info
    }

    public static void startRpc() throws Exception{
        try{
            WsServer.getInstance(ModuleE.EB)
                    .moduleRoles(new String[]{"1.0"})
                    .moduleVersion("1.0")
                    .scanPackage("io.nuls.eventbus.rpc.cmd")
                    .connect("ws://127.0.0.1:8887");
            CmdDispatcher.syncKernel();
        }catch (Exception e){
            Log.error("Event Bus rpc start up failed");
            Constants.THREAD_POOL.shutdownNow();
        }
    }

    public static void startProcessors(){
        ConcurrentMap<String, Topic> topics = EventBus.getInstance().getTopicMap();
        if(!topics.isEmpty()){
            Set<String> roles = topics.values().stream().flatMap(topic -> topic.getSubscribers().stream().map(subscriber -> subscriber.getModuleAbbr())).collect(Collectors.toSet());
            roles.stream().map(role -> EventBusRuntime.CLIENT_SYNC_QUEUE.offer(new Object[]{role,"subscribe"}));
        }
        Constants.THREAD_POOL.execute(new ClientSyncProcessor());
        Constants.THREAD_POOL.execute(new EventDispatchProcessor());
    }

    public static void initDB(){
        ebStorageService.init();
        ebStorageService.loadTopics();
    }

}
