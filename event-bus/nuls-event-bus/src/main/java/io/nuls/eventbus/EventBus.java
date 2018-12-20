package io.nuls.eventbus;

import io.nuls.eventbus.constant.EbConstants;
import io.nuls.eventbus.constant.EbErrorCode;
import io.nuls.eventbus.model.Subscriber;
import io.nuls.eventbus.model.Topic;
import io.nuls.tools.exception.NulsRuntimeException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author naveen
 */
public class EventBus {

    private static EventBus INSTANCE;

    private ConcurrentMap<String, Topic> topicMap = new ConcurrentHashMap<>();

    private EventBus(){}

    public static synchronized EventBus getInstance(){
        if(INSTANCE == null){
            INSTANCE = new EventBus();
        }
        return INSTANCE;
    }

    public void subscribe(Map<String,Object> params) throws NulsRuntimeException {
        String topicId = (String)params.get(EbConstants.CMD_PARAM_TOPIC);
        Subscriber subscriber = buildSubscriber(params);
        synchronized (this){
            if(topicMap.containsKey(topicId)){
                Topic topic = topicMap.get(topicId);
                topicMap.put(topicId,topic.addSubscriber(subscriber));
            }else{
                throw new NulsRuntimeException(EbErrorCode.TOPIC_NOT_FOUND);
            }
        }
    }
    public void unsubscribe(Map<String,Object> params) throws NulsRuntimeException{
        String topicId = (String)params.get(EbConstants.CMD_PARAM_TOPIC);
        Subscriber subscriber = buildSubscriber(params);
        synchronized (this){
            if(topicMap.containsKey(topicId)){
                Topic topic = topicMap.get(topicId);
                topicMap.put(topicId,topic.removeSubscriber(subscriber));
            }else{
                throw new NulsRuntimeException(EbErrorCode.TOPIC_NOT_FOUND);
            }
        }
    }

    public Set<Subscriber> publish(Map<String,Object> params){
        String topicId = (String)params.get(EbConstants.CMD_PARAM_TOPIC);
        String abbr = (String)params.get(EbConstants.CMD_PARAM_ROLE);
        String moduleName = (String)params.get(EbConstants.CMD_PARAM_ROLE_NAME);
        String domain = (String)params.get(EbConstants.CMD_PARAM_DOMAIN);
        Topic topic;
        synchronized (this){
            if(topicMap.containsKey(topicId)){
                topic = topicMap.get(topicId);
            }else{
                topic = new Topic(topicId,abbr,moduleName,domain);
                topicMap.put(topicId,topic);
            }
            return topic.getSubscribers();
        }
    }

    private Subscriber buildSubscriber(Map<String,Object> params){
        String abbr = (String)params.get(EbConstants.CMD_PARAM_ROLE);
        String callBackCmd = (String)params.get(EbConstants.CMD_PARAM_ROLE_CALLBACK);
        String moduleName = (String)params.get(EbConstants.CMD_PARAM_ROLE_NAME);
        String domain = (String)params.get(EbConstants.CMD_PARAM_DOMAIN);
        return new Subscriber(abbr,moduleName,domain, callBackCmd);
    }

    public Set<String> getAllSubscribers(){
        return topicMap.values().stream().flatMap(topic -> topic.getSubscribers().stream().map(subscriber -> subscriber.getModuleAbbr())).collect(Collectors.toSet());
    }

    public void setTopicMap(ConcurrentMap<String, Topic> topicMap) {
        this.topicMap = topicMap;
    }
}
