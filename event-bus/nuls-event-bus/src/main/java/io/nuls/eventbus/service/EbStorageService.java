package io.nuls.eventbus.service;

import io.nuls.eventbus.model.Topic;

import java.util.concurrent.ConcurrentMap;

/**
 * @author naveen
 */
public interface EbStorageService {

    /**
     *
     */
    void init();

    ConcurrentMap<String,Topic> loadTopics();

    void putTopic(Topic topic);

    Topic getTopic(byte[] key);

}
