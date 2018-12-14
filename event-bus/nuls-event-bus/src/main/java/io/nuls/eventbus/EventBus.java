package io.nuls.eventbus;

import io.nuls.eventbus.constant.EbErrorCode;
import io.nuls.eventbus.model.Subscriber;
import io.nuls.eventbus.model.Topic;
import io.nuls.tools.exception.NulsRuntimeException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author naveen
 */
public class EventBus {

    public static EventBus INSTANCE;

    private ConcurrentMap<String, Topic> topicMap = new ConcurrentHashMap<>();

    private EventBus(){}

    public static synchronized EventBus getInstance(){
        if(INSTANCE == null){
            INSTANCE = new EventBus();
        }
        return INSTANCE;
    }

    public void subscribe(Map<String,Object> params) throws NulsRuntimeException {
        String topicId = (String)params.get("topic");
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
        String topicId = (String)params.get("topic");
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
        String topicId = (String)params.get("topic");
        String abbr = (String)params.get("abbr");
        String moduleName = (String)params.get("moduleName");
        String domain = (String)params.get("domain");
        Topic topic = null;
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
        String abbr = (String)params.get("abbr");
        String moduleName = (String)params.get("moduleName");
        String domain = (String)params.get("domain");
        return new Subscriber(abbr,moduleName,domain);
    }

    public ConcurrentMap<String, Topic> getTopicMap() {
        return topicMap;
    }

    public void setTopicMap(ConcurrentMap<String, Topic> topicMap) {
        this.topicMap = topicMap;
    }
}
