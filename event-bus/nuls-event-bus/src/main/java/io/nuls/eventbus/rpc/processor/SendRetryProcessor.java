package io.nuls.eventbus.rpc.processor;

import io.nuls.eventbus.constant.EbConstants;
import io.nuls.eventbus.model.Subscriber;
import io.nuls.eventbus.rpc.invoke.EventAuditInvoke;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.tools.log.Log;

import java.util.Map;

/** Separate thread for each subscriber to perform retry process in case event data is not sent successfully.
 *  subscriber has to send acknowledgement for the retry process.
 * @author naveen
 */
public class SendRetryProcessor implements Runnable {

    private final Object[] subscriberEvent;

    SendRetryProcessor(Object[] obj){
        this.subscriberEvent = obj;
    }

    @Override
    public void run() {
        try{
            if(null != subscriberEvent){
                Subscriber subscriber = (Subscriber)subscriberEvent[0];
                Log.info("SendAndRetry thread running for Subscriber : "+subscriber.getModuleAbbr());
                Map<String,Object> params = (Map<String,Object>)subscriberEvent[1];
                String messageId = sendEvent(subscriber,params);
                int retryAttempt = 0;
                Log.debug("Acknowledgement for send event messageId: "+messageId +" received");
                while (retryAttempt <= EbConstants.EVENT_DISPATCH_RETRY_COUNT && messageId == null){
                    Thread.sleep(EbConstants.EVENT_RETRY_WAIT_TIME);
                    retryAttempt = retryAttempt + 1;
                    Log.debug("Retry for Subscriber : "+subscriber.getModuleAbbr() +" --> "+"Retry Attempt:"+retryAttempt);
                    messageId = sendEvent(subscriber,params);
                }
            }
        }catch (Exception e){
            Log.error(e.getMessage());
        }
    }

    private String sendEvent(Subscriber subscriber,Map<String,Object> params){
        try{
            return CmdDispatcher.requestAndInvokeWithAck(subscriber.getModuleAbbr(),subscriber.getCallBackCmd(),params,Constants.ZERO,Constants.ZERO,new EventAuditInvoke());
        }catch (Exception e){
            Log.error("Exception in sending event to subscriber :"+subscriber.getModuleAbbr()+" ->"+e.getMessage());
        }
        return null;
    }
}
