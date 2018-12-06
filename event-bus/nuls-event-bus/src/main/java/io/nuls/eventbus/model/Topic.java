package io.nuls.eventbus.model;

import io.nuls.rpc.model.ModuleE;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ToString
@NoArgsConstructor
public class Topic implements Serializable {

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

    public Topic(String topicId, String abbr,String name,String domain) {
        this.topicId = topicId;
        this.moduleAbbr = abbr;
        this.moduleName = name;
        this.domain = domain;
        this.createTime = System.currentTimeMillis();
        subscribers = new CopyOnWriteArraySet<Subscriber>();
    }

    public Topic addSubscriber(Subscriber subscriber){
        subscribers.add(subscriber);
        return this;
    }

    public Topic removeSubscriber(Subscriber subscriber){
        for(Subscriber sub : subscribers){
            if(sub.equals(subscriber)){
                subscribers.remove(sub);
            }
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || this.getClass() != o.getClass()) {return false;}
        Topic topic = (Topic) o;
        return Objects.equals(this.topicId, topic.topicId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.topicId);
    }
}
