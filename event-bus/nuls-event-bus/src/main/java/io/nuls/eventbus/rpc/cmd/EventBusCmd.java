package io.nuls.eventbus.rpc.cmd;

import io.nuls.eventbus.EventBus;
import io.nuls.eventbus.constant.EbConstants;
import io.nuls.eventbus.constant.EbErrorCode;
import io.nuls.eventbus.model.Subscriber;
import io.nuls.eventbus.rpc.processor.EventDispatchProcessor;
import io.nuls.eventbus.runtime.EventBusRuntime;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsRuntimeException;
import static io.nuls.eventbus.util.EbLog.Log;

import java.util.Map;
import java.util.Set;

/**
 * Collection of RPC commands for other modules/roles to interact with event bus
 *
 * @author naveen
 */
public class EventBusCmd extends BaseCmd {

    private final EventBus eventBus = EventBus.getInstance();

    /**
     * subscription command for modules to subscribe given topic
     * It adds subscribed role to the client sync queue to get connection info from kernel
     *
     * @param params required parameters for the subscription
     * @return success response when subscription is success, failure response when required parameters are missing or topic is not found
     */
    @CmdAnnotation(cmd = EbConstants.EB_SUBSCRIBE, version = 1.0, description = "Subscribe to specific topic")
    @Parameter(parameterName = EbConstants.CMD_PARAM_ROLE, parameterType = "String")
    @Parameter(parameterName = EbConstants.CMD_PARAM_ROLE_NAME, parameterType = "String")
    @Parameter(parameterName = EbConstants.CMD_PARAM_DOMAIN, parameterType = "String")
    @Parameter(parameterName = EbConstants.CMD_PARAM_TOPIC, parameterType = "String")
    @Parameter(parameterName = EbConstants.CMD_PARAM_ROLE_CALLBACK, parameterType = "String")
    public Object subscribe(Map<String,Object> params){
        if(params == null){
            return failed(EbErrorCode.PARAMS_MISSING);
        }
        String moduleAbbr = (String)params.get(EbConstants.CMD_PARAM_ROLE);
        String topic = (String)params.get(EbConstants.CMD_PARAM_TOPIC);
        String callBackCmd = (String)params.get(EbConstants.CMD_PARAM_ROLE_CALLBACK);
        if(StringUtils.isBlank(topic) || StringUtils.isBlank(moduleAbbr) || StringUtils.isBlank(callBackCmd)){
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

    /**
     * Un subscription command for modules to unscubscribe from a topic
     *
     * @param params parameters required for the operation
     * @return success/failure response
     */
    @CmdAnnotation(cmd = EbConstants.EB_UNSUBSCRIBE, version = 1.0, description = "UnSubscribe to specific topic")
    @Parameter(parameterName = EbConstants.CMD_PARAM_TOPIC, parameterType = "String")
    @Parameter(parameterName = EbConstants.CMD_PARAM_ROLE, parameterType = "String")
    public Object unsubscribe(Map<String,Object> params){
        if(params == null){
            return failed(EbErrorCode.PARAMS_MISSING);
        }
        String moduleAbbr = (String)params.get(EbConstants.CMD_PARAM_ROLE);
        String topic = (String)params.get(EbConstants.CMD_PARAM_TOPIC);
        if(StringUtils.isBlank(topic) || StringUtils.isBlank(moduleAbbr)){
            return failed(EbErrorCode.PARAMS_MISSING);
        }
        try{
            eventBus.unsubscribe(params);
        }catch (NulsRuntimeException nre){
            Log.error("UnSubscribe is failed");
            return failed(nre.getErrorCode());
        }
        EventBusRuntime.CLIENT_SYNC_QUEUE.offer(new Object[]{moduleAbbr, EbConstants.UNSUBSCRIBE});
        return success();
    }

    /**
     * Command to publish/send the event to a topic
     * If given topic is noot found at Event Bus, it creates new one
     * Adds event and subscribers to event dispatch Queue to handle separate thread
     * @param params required parameters for the command
     * @return success/failure response
     */
    @CmdAnnotation(cmd = EbConstants.EB_SEND, version = 1.0, description = "Publish the event data to subscribers")
    @Parameter(parameterName = EbConstants.CMD_PARAM_TOPIC, parameterType = "String")
    @Parameter(parameterName = EbConstants.CMD_PARAM_ROLE, parameterType = "String")
    @Parameter(parameterName = EbConstants.CMD_PARAM_ROLE_NAME, parameterType = "String")
    @Parameter(parameterName = EbConstants.CMD_PARAM_DOMAIN, parameterType = "String")
    @Parameter(parameterName = EbConstants.CMD_PARAM_DATA, parameterType = "Object")
    public Object send(Map<String,Object> params){
        if(params == null){
            return failed(EbErrorCode.PARAMS_MISSING);
        }
        Object data = params.get(EbConstants.CMD_PARAM_DATA);
        String topic = (String)params.get(EbConstants.CMD_PARAM_TOPIC);
        if(StringUtils.isBlank(topic)){
            return failed(EbErrorCode.PARAMS_MISSING);
        }
        Set<Subscriber> subscribers = eventBus.publish(params);
        if(!subscribers.isEmpty()){
            EventBusRuntime.EVENT_DISPATCH_QUEUE.offer(new Object[]{data,subscribers});
            EbConstants.EB_THREAD_POOL.execute(new EventDispatchProcessor());
        }
        return success();
    }
}
