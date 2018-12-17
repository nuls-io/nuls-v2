package io.nuls.eventbus.rpc.cmd;

import io.nuls.eventbus.EventBus;
import io.nuls.eventbus.constant.EbConstants;
import io.nuls.eventbus.constant.EbErrorCode;
import io.nuls.eventbus.model.Subscriber;
import io.nuls.eventbus.rpc.processor.EventDispatchProcessor;
import io.nuls.eventbus.runtime.EventBusRuntime;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

import java.util.Map;
import java.util.Set;

/**
 * @author naveen
 */
public class EventBusCmd extends BaseCmd {

    private EventBus eventBus = EventBus.getInstance();

    @CmdAnnotation(cmd = "eb_subscribe", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "Subscribe to specific topic")
    public Object subscribe(Map<String,Object> params) throws Exception{
        if(params == null){
            return failed(EbErrorCode.PARAMS_MISSING);
        }
        String moduleAbbr = (String)params.get("abbr");
        String topic = (String)params.get("topic");
        if(topic == null || moduleAbbr == null){
            return failed(EbErrorCode.PARAMS_MISSING);
        }
        try{
            eventBus.subscribe(params);
        }catch (NulsRuntimeException nre){
            Log.error("Subscription is failed");
            return failed(nre.getErrorCode());
        }
        EventBusRuntime.CLIENT_SYNC_QUEUE.offer(new Object[]{moduleAbbr, EbConstants.SUBSCRIBE});
        return success();
    }

    @CmdAnnotation(cmd = "eb_unsubscribe", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "UnSubscribe to specific topic")
    public Object unsubscribe(Map<String,Object> params) throws Exception{
        if(params == null){
            return failed(EbErrorCode.PARAMS_MISSING);
        }
        String moduleAbbr = (String)params.get("abbr");
        String topic = (String)params.get("topic");
        if(topic == null || moduleAbbr == null){
            return failed(EbErrorCode.PARAMS_MISSING);
        }
        try{
            eventBus.unsubscribe(params);
        }catch (NulsRuntimeException nre){
            Log.error("Subscription is failed");
            return failed(nre.getErrorCode());
        }
        EventBusRuntime.CLIENT_SYNC_QUEUE.offer(new Object[]{moduleAbbr, EbConstants.UNSUBSCRIBE});
        return success();
    }

    @CmdAnnotation(cmd = "eb_send", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "Publish the event data to subscribers")
    public Object send(Map<String,Object> params) throws Exception{
        if(params == null){
            return failed(EbErrorCode.PARAMS_MISSING);
        }
        Object data = params.get("data");
        String topic = (String)params.get("topic");
        if(topic == null){
            return failed(EbErrorCode.PARAMS_MISSING);
        }
        String messageId = (String)params.get("messageId");
        Set<Subscriber> subscribers = eventBus.publish(params);
        if(!subscribers.isEmpty()){
            EventBusRuntime.EVENT_DISPATCH_QUEUE.offer(new Object[]{data,subscribers,messageId});
            Constants.THREAD_POOL.execute(new EventDispatchProcessor());
        }
        return success();
    }
}
