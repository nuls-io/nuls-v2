package io.nuls.eventbus;

import io.nuls.eventbus.model.Subscriber;
import io.nuls.eventbus.model.Topic;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EventBus {

    public static EventBus INSTANCE;

    private ConcurrentMap<String, Topic> topicMap = new ConcurrentHashMap<>();

    private EventBus(){}

    public static EventBus getInstance(){

        if(INSTANCE == null){
            synchronized (EventBus.class){
                if(INSTANCE == null){
                    INSTANCE = new EventBus();
                }
            }
        }
        return INSTANCE;
    }

    public int subscribe(Map<String,Object> params){
        String topicId = (String)params.get("topic");
        Subscriber subscriber = buildSubscriber(params);
        synchronized (this){
            if(topicMap.containsKey(topicId)){
                Topic topic = topicMap.get(topicId);
                topicMap.put(topicId,topic.addSubscriber(subscriber));
            }else{
                //TODO change this  with proper code return
                return 1;
            }
        }
        //TODO change this  with proper code return
        return 0;
    }
    public int unsubscribe(Map<String,Object> params){
        String topicId = (String)params.get("topic");
        Subscriber subscriber = buildSubscriber(params);
        synchronized (this){
            if(topicMap.containsKey(topicId)){
                Topic topic = topicMap.get(topicId);
                topicMap.put(topicId,topic.removeSubscriber(subscriber));
            }else{
                return 1;
            }
        }
        return 0;
    }

    public Set<Subscriber> publish(Map<String,Object> params){
        String topicId = (String)params.get("topic");
        String abbr = (String)params.get("abbr");
        String moduleName = (String)params.get("moduleName");
        String domain = (String)params.get("domain");
        Object data = params.get("data");
        Topic topic = null;

        synchronized (this){
            if(topicMap.containsKey(topicId)){
                topic = topicMap.get(topicId);
            }else{
                topic = new Topic(topicId,abbr,moduleName,domain);
                topicMap.put(topicId,topic);
            }
        }
        return topic.getSubscribers();
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
