package io.nuls.eventbus.rpc.cmd;

import io.nuls.eventbus.EventBus;
import io.nuls.eventbus.constant.EBConstants;
import io.nuls.eventbus.model.Subscriber;
import io.nuls.eventbus.rpc.processor.EventBusRuntime;
import io.nuls.eventbus.rpc.processor.EventDispatchProcessor;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.exception.NulsException;

import java.util.Map;
import java.util.Set;

public class EventBusCmd extends BaseCmd {

    private EventBus eventBus = EventBus.getInstance();

    @CmdAnnotation(cmd = "eb_subscribe", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "Subscribe to specific topic")
    public Object subscribe(Map<String,Object> params) throws NulsException{
        if(params == null){
            //TODO  add proper error code
            throw new NulsException(new ErrorCode());
        }
        String messageId = (String)params.get("messageId");
        String moduleAbbr = (String)params.get("abbr");
        EventBusRuntime.CLIENT_SYNC_QUEUE.offer(new Object[]{moduleAbbr, EBConstants.SUBSCRIBE});
        int status = eventBus.subscribe(params);
        return success();
    }

    @CmdAnnotation(cmd = "eb_unsubscribe", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "UnSubscribe to specific topic")
    public Object unsubscribe(Map<String,Object> params) throws NulsException{
        if(params == null){
            //TODO  add proper error code
            throw new NulsException(new ErrorCode());
        }
        String messageId = (String)params.get("messageId");
        String moduleAbbr = (String)params.get("abbr");
        EventBusRuntime.CLIENT_SYNC_QUEUE.offer(new Object[]{moduleAbbr,EBConstants.UNSUBSCRIBE});
        int status = eventBus.unsubscribe(params);
        return success();
    }

    @CmdAnnotation(cmd = "eb_send", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "Publish the event data to subscribers")
    public Object send(Map<String,Object> params) throws NulsException{
        if(params == null){
            //TODO  add proper error code
            throw new NulsException(new ErrorCode());
        }
        Object data = params.get("data");
        String messageId = (String)params.get("messageId");
        Set<Subscriber> subscribers = eventBus.publish(params);

        if(!subscribers.isEmpty()){
            EventBusRuntime.EVENT_DISPATCH_QUEUE.offer(new Object[]{data,subscribers,messageId});
            Constants.THREAD_POOL.execute(new EventDispatchProcessor());
        }

        return success();
    }
}
