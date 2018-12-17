package io.nuls.eventbus.rpc.processor;

import io.nuls.eventbus.constant.EbConstants;
import io.nuls.eventbus.runtime.EventBusRuntime;
import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.client.runtime.ClientRuntime;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Ack;
import io.nuls.rpc.model.message.Message;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;

import java.nio.channels.NotYetConnectedException;
import java.util.Map;

/** Separate thread for each subscriber to perform retry process in case event data is not sent successfully.
 *  subscriber has to send acknowledgement for the retry process.
 * @author naveen
 */
public class RetryProcessor implements Runnable {

    @Override
    public void run() {
        try{
            Log.info("Retry thread running..");
            Object[] objects = EventBusRuntime.firstObjArrInRetryQueue();
            if(null != objects){
                Message rspMessage = (Message)objects[0];
                WsClient wsClient = (WsClient)objects[1];
                String role = (String)objects[2];
                int retryAttempt = 0;
                boolean ackResponse = receiveAck(rspMessage.getMessageId());
                Log.debug("Acknowledgement for send event messageId: "+rspMessage.getMessageId() +" --> "+ackResponse);
                while (retryAttempt <= EbConstants.EVENT_DISPATCH_RETRY_COUNT && !ackResponse){
                    retryAttempt = retryAttempt + 1;
                    Log.debug("Retry for Subscriber : "+role +" --> "+"Retry Attempt:"+retryAttempt);
                    Thread.sleep(EbConstants.EVENT_RETRY_WAIT_TIME);
                    try{
                        wsClient.send(JSONUtils.obj2json(rspMessage));
                        ackResponse = receiveAck(rspMessage.getMessageId());
                    }catch(NotYetConnectedException nyce){
                        ackResponse = false;
                        EventBusRuntime.CLIENT_SYNC_QUEUE.offer(new Object[]{role, EbConstants.SUBSCRIBE});
                    }
                }
            }
        }catch (Exception e){
            Log.error(e.getMessage());
        }
    }

    private static boolean receiveAck(String messageId) throws Exception {
        long timeMillis = TimeService.currentTimeMillis();
        while (TimeService.currentTimeMillis() - timeMillis <= Constants.TIMEOUT_TIMEMILLIS) {
            Message message = ClientRuntime.firstMessageInAckQueue();
            if (message == null) {
                /*Thread.sleep(Constants.INTERVAL_TIMEMILLIS);*/
                continue;
            }
            Ack ack = JSONUtils.map2pojo((Map) message.getMessageData(), Ack.class);
            if (ack.getRequestId().equals(messageId)) {
                return true;
            }
            ClientRuntime.ACK_QUEUE.offer(message);
            /*Thread.sleep(Constants.INTERVAL_TIMEMILLIS);*/
        }
        return false;
    }
}
