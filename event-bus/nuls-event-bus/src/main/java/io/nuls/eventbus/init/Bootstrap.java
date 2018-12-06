package io.nuls.eventbus.init;



import io.nuls.eventbus.rpc.processor.ClientSyncProcessor;
import io.nuls.eventbus.rpc.processor.EventDispatchProcessor;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;

/**
 * @author naveen
 */
public class Bootstrap {

    public static void main(String args[]){
        Log.info("Event Bus module bootstrap starts...");
        try {
            init();
            startRpc();
            TimeService.getInstance().start();
            // TODO register Event Bus commands

        }catch (Exception e){
            Log.error("Event Bus module Bootstrap failed..exiting the system");
            System.exit(1);
        }
    }

    public static void init(){
        // TODO initialize module info and retrieve stored topic info from file system/RocksDB
    }

    public static void startRpc() throws Exception{
        try{
            WsServer.getInstance(ModuleE.EB)
                    .moduleRoles(new String[]{"1.0"})
                    .moduleVersion("1.0")
                    .scanPackage("io.nuls.eventbus.rpc.cmd")
                    .connect("ws://127.0.0.1:8887");
            CmdDispatcher.syncKernel();
            Constants.THREAD_POOL.execute(new ClientSyncProcessor());
            Constants.THREAD_POOL.execute(new EventDispatchProcessor());
        }catch (Exception e){
            Log.error("Event Bus rpc start up failed");
            Constants.THREAD_POOL.shutdownNow();
        }
    }
}
