package io.nuls.eventbus;

import io.nuls.eventbus.constant.EbConstants;
import io.nuls.eventbus.constant.EbErrorCode;
import io.nuls.eventbus.model.Subscriber;
import io.nuls.eventbus.model.Topic;
import io.nuls.eventbus.service.EbStorageService;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.exception.NulsRuntimeException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * The {@code EventBus} class represents all operations related to Event Bus
 *  <p>It is implementation of publish subscribe pattern where publisher publishes/sends
 *   event to {@code EventBus} and {@code EventBus} sends/notifies the subscribers.
 *  </p>
 *  <p>It is singleton class to represent for entire Event Bus module</p>
 *  <p> Any valid module/role in NULS blockchain can be a publisher or subscriber</p>
 *
 * @author naveen
 * @since 1.0
 */
public class EventBus {

    private static EventBus INSTANCE;

    private ConcurrentMap<String, Topic> topicMap = new ConcurrentHashMap<>();

    private final EbStorageService ebStorageService;

    private EventBus(){
        this.ebStorageService = SpringLiteContext.getBean(EbStorageService.class);
    }

    /**
     * EventBus is a singleton class to have single object for entire event bus module
     * @return singleton instance of EventBus
     */
    public static synchronized EventBus getInstance(){
        if(INSTANCE == null){
            INSTANCE = new EventBus();
        }
        return INSTANCE;
    }

    /**
     * Any role/module can subscribe to given topic
     * @param params parameters needed for subscription operation
     * @throws NulsRuntimeException when subscription fails
     */
    public void subscribe(Map<String,Object> params) throws NulsRuntimeException {
        String topicId = (String)params.get(EbConstants.CMD_PARAM_TOPIC);
        Topic topic;
        if((topic = topicMap.get(topicId)) != null){
            synchronized (this){
                topicMap.put(topicId,topic.addSubscriber(buildSubscriber(params)));
                ebStorageService.putTopic(topic);
            }
        }else{
            throw new NulsRuntimeException(EbErrorCode.TOPIC_NOT_FOUND);
        }
    }

    /**
     * Any role/module can unsubscribe from given topic
     * @param params required parameters for unsubscribe operation
     * @throws NulsRuntimeException when operation fails
     */
    public void unsubscribe(Map<String,Object> params) throws NulsRuntimeException{
        String topicId = (String)params.get(EbConstants.CMD_PARAM_TOPIC);
        Topic topic;
        if((topic = topicMap.get(topicId)) != null){
            synchronized (this){
                topicMap.put(topicId,topic.removeSubscriber(buildSubscriber(params)));
                ebStorageService.putTopic(topic);
            }
        }else{
            throw new NulsRuntimeException(EbErrorCode.TOPIC_NOT_FOUND);
        }
    }

    /**
     * Any module/role sends event to a topic, EventBus sends the event to subscribers of that topic
     * If specified topic is already not present, it creates and stores in DB
     * @param params parameters required to create/publish to a topic
     * @return list of subscribers who subscribed to given topic
     */
    public Set<Subscriber> publish(Map<String,Object> params){
        String topicId = (String)params.get(EbConstants.CMD_PARAM_TOPIC);
        String abbr = (String)params.get(EbConstants.CMD_PARAM_ROLE);
        String moduleName = (String)params.get(EbConstants.CMD_PARAM_ROLE_NAME);
        String domain = (String)params.get(EbConstants.CMD_PARAM_DOMAIN);
        Topic topic;
        if((topic = topicMap.get(topicId)) == null){
            synchronized (this){
                if((topic = topicMap.get(topicId)) == null){
                    topic = new Topic(topicId,abbr,moduleName,domain);
                    topicMap.put(topicId,topic);
                    ebStorageService.putTopic(topic);
                }
            }
        }
        return topic.getSubscribers();
    }

    private Subscriber buildSubscriber(Map<String,Object> params){
        String abbr = (String)params.get(EbConstants.CMD_PARAM_ROLE);
        String callBackCmd = (String)params.get(EbConstants.CMD_PARAM_ROLE_CALLBACK);
        String moduleName = (String)params.get(EbConstants.CMD_PARAM_ROLE_NAME);
        String domain = (String)params.get(EbConstants.CMD_PARAM_DOMAIN);
        return new Subscriber(abbr,moduleName,domain, callBackCmd);
    }

    /**
     * Get all subscribers from all topics
     * @return list of subscribers from all topics
     */
    public Set<String> getAllSubscribers(){
        return topicMap.values().stream().flatMap(topic -> topic.getSubscribers().stream().map(subscriber -> subscriber.getModuleAbbr())).collect(Collectors.toSet());
    }

    /**
     * set the topics to topic map
     * @param topicMap
     */
    public void setTopicMap(ConcurrentMap<String, Topic> topicMap) {
        this.topicMap = topicMap;
    }
}
