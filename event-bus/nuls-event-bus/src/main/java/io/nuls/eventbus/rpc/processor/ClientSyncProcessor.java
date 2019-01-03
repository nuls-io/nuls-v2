package io.nuls.eventbus.rpc.processor;


import io.nuls.eventbus.EventBus;
import io.nuls.eventbus.constant.EbConstants;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.client.runtime.ClientRuntime;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.server.runtime.ServerRuntime;
import io.nuls.tools.parse.JSONUtils;

import static io.nuls.eventbus.util.EbLog.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread to sync roles connection information
 * <p> When any role subscribe to a topic, role is kept in the client sync Queue.
 *     It connects to Kernel modules and gets connection information for the role and stores at event bus runtime</p>
 *  <p>If operation is unsubscribe, it removed the role from role map from event bus if the role is no more subscribed any topic</p>
 * @author naveen
 */
public class ClientSyncProcessor implements Runnable {

    private Object[] objects;

    public ClientSyncProcessor(Object[] objects) {
        this.objects = objects;
    }

    /**
     * Thread to sync role connection information from kernel
     */
    @Override
    public void run() {
        try{
            String moduleAbbr = (String)objects[0];
            String cmd = (String)objects[1];
            Log.info("Sync process started for Subscriber :"+moduleAbbr +" for the operation:"+cmd);
            switch (cmd){
                case EbConstants.SUBSCRIBE:
                    //syncRoleConnectionInfo(moduleAbbr);
                    getRoleConnectionInfo(moduleAbbr);
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
        }catch (Exception e){
            Log.error(e);
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
                //TODO parse data to get connection information
                ClientRuntime.ROLE_MAP.put(subscriber,connectionInfoMap);
                ClientRuntime.WS_CLIENT_MAP.remove(ClientRuntime.getRemoteUri(subscriber));
                ClientRuntime.getWsClient(ClientRuntime.getRemoteUri(subscriber));
            }else{
                Log.error(response.getResponseComment());
            }
        }catch (Exception e){
            Log.error("Couldn't get connection info from kernel for the role:"+subscriber);
        }
    }
    private void getRoleConnectionInfo(String subscriber){
        Log.info("Get connection info for the role :"+subscriber);
        try{
            Response response = CmdDispatcher.requestAndResponse(ModuleE.KE.abbr,"registerAPI",JSONUtils.json2map(JSONUtils.obj2json(ServerRuntime.LOCAL)));
            if(response.isSuccess()){
                Map responseData = (Map)response.getResponseData();
                Map methodMap = (Map)responseData.get("registerAPI");
                Map dependMap = (Map)methodMap.get("Dependencies");
                for(Object obj : dependMap.entrySet()){
                    Map.Entry<String,Map> entry = (Map.Entry<String,Map>)obj;
                    if(entry.getKey().equals(subscriber)){
                        ClientRuntime.ROLE_MAP.put(entry.getKey(), entry.getValue());
                        //connect to the role with latest connection info
                        ClientRuntime.WS_CLIENT_MAP.remove(ClientRuntime.getRemoteUri(subscriber));
                        ClientRuntime.getWsClient(ClientRuntime.getRemoteUri(subscriber));
                        break;
                    }
                }
            }
        }catch (Exception e){
            Log.error(e.getMessage());
        }
    }
}
