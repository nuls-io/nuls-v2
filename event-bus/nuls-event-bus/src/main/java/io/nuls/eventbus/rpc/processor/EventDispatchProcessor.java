package io.nuls.eventbus.rpc.processor;

import io.nuls.eventbus.constant.EbConstants;
import io.nuls.eventbus.model.Subscriber;
import io.nuls.eventbus.runtime.EventBusRuntime;
import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.client.runtime.ClientRuntime;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.MessageUtil;
import io.nuls.rpc.model.message.Response;

import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.nio.channels.NotYetConnectedException;
import java.util.Set;

/**
 * @author naveen
 */
public class EventDispatchProcessor implements Runnable {

    @Override
    public void run() {
        try{
            Log.info("Processing the published event starts..");
            Object[] objects = EventBusRuntime.firstObjArrInEventDispatchQueue();
            if(null != objects){
                Object data = objects[0];
                Set<Subscriber> subscribers =(Set<Subscriber>) objects[1];
                //TODO does messageID needs to be taken from subscriber ? if yes, it has to be stored while subscription
                String messageId = (String)objects[2];
                for (Subscriber subscriber : subscribers){
                    String url = ClientRuntime.getRemoteUri(subscriber.getModuleAbbr());
                    WsClient wsClient = ClientRuntime.getWsClient(url);
                    Message rspMessage = buildMessage(data,messageId);
                    EventBusRuntime.RETRY_QUEUE.offer(new Object[]{rspMessage,wsClient,subscriber.getModuleAbbr()});
                    try{
                        Log.debug("Sending event to subscriber :"+subscriber.getModuleAbbr());
                        wsClient.send(JSONUtils.obj2json(rspMessage));
                        //span separate thread to retry for each subscriber
                        EbConstants.RETRY_THREAD_POOL.execute(new RetryProcessor());
                    }catch (NotYetConnectedException nyce){
                        Log.error("Client is not connected yet : "+url);
                        EventBusRuntime.CLIENT_SYNC_QUEUE.offer(new Object[]{subscriber.getModuleAbbr(), EbConstants.SUBSCRIBE});
                        EbConstants.RETRY_THREAD_POOL.execute(new RetryProcessor());
                    }
                }
                Log.info("Processing the published event Ends..");
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
