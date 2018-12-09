package io.nuls.eventbus.rpc.processor;

import io.nuls.eventbus.model.Subscriber;
import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.client.runtime.ClientRuntime;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.MessageUtil;
import io.nuls.rpc.model.message.Response;

import io.nuls.rpc.server.runtime.ServerRuntime;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.util.Set;

public class EventDispatchProcessor implements Runnable {

    @Override
    public void run() {
        try{
            System.out.println("Processing the event");
            Object[] objects = EventBusRuntime.firstObjArrInEventDispatchQueue();
            if(null == objects){
                Thread.sleep(200L);
            }else{
                Object data = objects[0];
                Set<Subscriber> subscribers =(Set<Subscriber>) objects[1];
                //TODO does messageID needs to be taken from subscriber ? if yes, it has to be stored while subscription
                String messageId = (String)objects[2];
                for (Subscriber subscriber : subscribers){
                    String url = ClientRuntime.getRemoteUri(subscriber.getModuleAbbr());
                    WsClient wsClient = ClientRuntime.getWsClient(url);

                   //TODO add retry logic in case of failure in delivery
                    wsClient.send(JSONUtils.obj2json(buildMessage(data,messageId)));
                }
                Thread.sleep(200L);
            }
        }catch (Exception e){
            Log.error(e);
        }
    }

    private Message buildMessage(Object data,String messageId){
        Message rspMessage = MessageUtil.basicMessage(MessageType.Response);
        Response response = MessageUtil.newResponse(messageId,Constants.BOOLEAN_TRUE,"");
        response.setResponseData(data);
        rspMessage.setMessageData(response);
        return rspMessage;
    }
}
