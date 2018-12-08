package io.nuls.eventbus.rpc.processor;

import io.nuls.eventbus.model.Subscriber;
import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.server.ServerRuntime;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.util.Set;

public class EventDispatchProcessor implements Runnable {

    @Override
    public void run() {
        try{
            Object[] objects = EventBusRuntime.firstObjArrInEventDispatchQueue();
            if(null == objects){
                Thread.sleep(1000L);
            }else{
                Object data = objects[0];
                Set<Subscriber> subscribers =(Set<Subscriber>) objects[1];
                //TODO does messageID needs to be taken from subscriber ? if yes, it has to be stored while subscription
                String messageId = (String)objects[2];
                Message rspMessage = Constants.basicMessage(Constants.nextSequence(), MessageType.Response);
                Response response = ServerRuntime.newResponse(messageId,Constants.booleanString(true),"");
                response.setResponseData(data);
                for (Subscriber subscriber : subscribers){
                    WsClient wsClient = EventBusRuntime.getWsClient(subscriber.getUrl());
                   //TODO add retry logic in case of failure in delivery
                    wsClient.send(JSONUtils.obj2json(data));
                }

            }
        }catch (Exception e){
            Log.error(e);
        }
    }
}
