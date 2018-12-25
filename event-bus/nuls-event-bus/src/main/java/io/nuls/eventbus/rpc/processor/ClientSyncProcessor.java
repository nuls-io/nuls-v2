package io.nuls.eventbus.rpc.processor;


import io.nuls.eventbus.EventBus;
import io.nuls.eventbus.constant.EbConstants;
import io.nuls.eventbus.runtime.EventBusRuntime;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.client.runtime.ClientRuntime;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.log.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author naveen
 */
public class ClientSyncProcessor implements Runnable {

    @Override
    public void run() {
        while (true){
            try{
                Object[] objects = EventBusRuntime.firstObjArrInClientSyncQueue();
                if(null == objects){
                    Thread.sleep(200L);
                }else{
                    String moduleAbbr = (String)objects[0];
                    String cmd = (String)objects[1];
                    Log.info("Sync process started for Subscriber :"+moduleAbbr +" for the operation:"+cmd);
                    switch (cmd){
                        case EbConstants.SUBSCRIBE:
                            syncRoleConnectionInfo(moduleAbbr);
                            break;
                        case EbConstants.UNSUBSCRIBE:
                            Set<String> roles = EventBus.getInstance().getAllSubscribers();
                            if(roles != null && !roles.contains(moduleAbbr)){
                                ClientRuntime.WS_CLIENT_MAP.remove(ClientRuntime.getRemoteUri(moduleAbbr));
                                ClientRuntime.ROLE_MAP.remove(moduleAbbr);
                            }
                            break;
                         default:
                    }
                    Thread.sleep(200L);
                }
            }catch (Exception e){
                Log.error(e);
            }
        }
    }

    private void syncRoleConnectionInfo(String subscriber){
        Map<String,Object> params = new HashMap<>(1);
        params.put(EbConstants.CMD_PARAM_ROLE,subscriber);
        try {
            //TODO update with actual command from kernel for role connection info
            Response response = CmdDispatcher.requestAndResponse(ModuleE.KE.abbr,"roleInfo",params);
            if(response.isSuccess()){
                ConcurrentMap<String,String> connectionInfoMap = new ConcurrentHashMap<>(2);
                Object data = response.getResponseData();
                //parse data to get connection information
                ClientRuntime.ROLE_MAP.put(subscriber,connectionInfoMap);
                ClientRuntime.getWsClient(ClientRuntime.getRemoteUri(subscriber));
            }else{
                Log.error(response.getResponseComment());
            }
        }catch (Exception e){
            Log.error("Couldn't get connection info from kernel for the role:"+subscriber);
        }
    }
}
