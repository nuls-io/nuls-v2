package io.nuls.eventbus.rpc.processor;


import io.nuls.eventbus.constant.EBConstants;
import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.info.Constants;
import io.nuls.tools.log.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClientSyncProcessor implements Runnable {

    @Override
    public void run() {

        while (true){
            try{
                Object[] objects = EventBusRuntime.firstObjArrInClientSyncQueue();
                if(null == objects){
                    Thread.sleep(1000L);
                }else{
                    String moduleAbbr = (String)objects[0];
                    String cmd = (String)objects[1];
                    ConcurrentMap<String,String> connectionInfoMap = new ConcurrentHashMap<>();
                    //TODO get module connection info from manager module, send request to retrieve
                    connectionInfoMap.put(Constants.KEY_IP,"127.0.0.1");
                    connectionInfoMap.put(Constants.KEY_PORT,"8876");
                    switch (cmd){
                        case "subscribe":
                            EventBusRuntime.subscribedRoleInfoMap.put(moduleAbbr,connectionInfoMap);
                            String url = EventBusRuntime.getRemoteUri(moduleAbbr);
                            EventBusRuntime.subscribedClientMap.put(url,EventBusRuntime.getWsClient(url));
                            break;
                        case "unsubscribe":
                            EventBusRuntime.subscribedClientMap.remove(EventBusRuntime.getRemoteUri(moduleAbbr));
                            EventBusRuntime.subscribedRoleInfoMap.remove(moduleAbbr);
                            break;
                         default:

                    }
                    Thread.sleep(1000L);
                }
            }catch (Exception e){
                Log.error(e);
            }
        }
    }
}
