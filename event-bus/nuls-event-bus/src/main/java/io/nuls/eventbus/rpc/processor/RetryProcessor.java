package io.nuls.eventbus.rpc.processor;

import io.nuls.eventbus.constant.EBConstants;
import io.nuls.rpc.client.WsClient;
import io.nuls.rpc.client.runtime.ClientRuntime;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.message.Ack;
import io.nuls.rpc.model.message.Message;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;

import java.util.Map;

public class RetryProcessor implements Runnable {

    @Override
    public void run() {
        try{
            Object[] objects = EventBusRuntime.firstObjArrInRetryQueue();
            if(null != objects){
                Message rspMessage = (Message)objects[0];
                WsClient wsClient = (WsClient)objects[1];
                int retryAttempt = 1;
                boolean ackResponse = receiveAck(rspMessage.getMessageId());
                while (retryAttempt <= EbConstants.EVENT_DISPATCH_RETRY_COUNT && !ackResponse){
                    retryAttempt = retryAttempt + 1;
                    Thread.sleep(EbConstants.EVENT_RETRY_WAIT_TIME);
                    try{
                        wsClient.send(rspMessage);
                        ackResponse = receiveAck(rspMessage.getMessageId());
                    }catch(NotYetConnectedException nyce){
                        ackResponse = false;
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
            /*
            获取队列中的第一个对象，如果是空，舍弃
            Get the first item of the queue, If it is an empty object, discard
             */
            Message message = ClientRuntime.firstMessageInAckQueue();
            if (message == null) {
                Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
                continue;
            }

            Ack ack = JSONUtils.map2pojo((Map) message.getMessageData(), Ack.class);
            if (ack.getRequestId().equals(messageId)) {
                /*
                messageId匹配，说明就是需要的结果，返回
                If messageId is the same, then the ack is needed
                 */
                return true;
            }

            /*
            messageId不匹配，放回队列
            Add back to the queue
             */
            ClientRuntime.ACK_QUEUE.offer(message);

            Thread.sleep(Constants.INTERVAL_TIMEMILLIS);
        }
        return false;
    }
}
