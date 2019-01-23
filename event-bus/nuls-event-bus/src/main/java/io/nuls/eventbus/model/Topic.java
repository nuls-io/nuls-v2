package io.nuls.eventbus.model;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents a topic in event bus
 * Modules/roles subscribes/unsubscribes to topic
 * Event is published to topic
 *
 * @author naveen
 */
@ToString
@NoArgsConstructor
public class Topic implements Serializable {

    private static final long serialVersionUID = -3703883040872615403L;

    @Getter
    @Setter
    private String topicId;

    @Getter
    @Setter
    private String topicDescription;

    @Getter
    @Setter
    private String moduleAbbr;

    @Getter
    @Setter
    private String moduleName;

    @Getter
    @Setter
    private String domain;

    @Getter
    @Setter
    private long createTime;

    @Getter
    @Setter
    private Set<Subscriber> subscribers;

    /**
     * @param topicId name of the topic
     * @param abbr module/role code who creates the topic
     * @param name name of the module/role who creates the topic
     * @param domain domain of the module/role who creates the topic
     */
    public Topic(String topicId, String abbr,String name,String domain) {
        this.topicId = topicId;
        this.moduleAbbr = abbr;
        this.moduleName = name;
        this.domain = domain;
        this.createTime = System.currentTimeMillis();
        subscribers = new CopyOnWriteArraySet<>();
    }

    /**
     * adds subscriber to the topic
     * @param subscriber
     * @return Topic
     */
    public Topic addSubscriber(Subscriber subscriber){
        subscribers.add(subscriber);
        return this;
    }

    /**
     * unsubscribe the role from the topic
     *
     * @param subscriber
     * @return
     */
    public Topic removeSubscriber(Subscriber subscriber){
        for(Subscriber sub : subscribers){
            if(sub.equals(subscriber)){
                subscribers.remove(sub);
            }
        }
        return this;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || this.getClass() != o.getClass()) {return false;}
        Topic topic = (Topic) o;
        return Objects.equals(this.topicId, topic.topicId);
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.topicId);
    }
}
