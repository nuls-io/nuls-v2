package io.nuls.eventbus.service;

import io.nuls.eventbus.model.Topic;

import java.util.concurrent.ConcurrentMap;

/**
 * @author naveen
 */
public interface EbStorageService {

    /**
     * Initializes Rocks DB
     * Creates table for Topic store
     */
    void init();

    /**
     * loads topics from RcoskDB table
     * @return ConcurrentMap<String,Topic>
     */
    ConcurrentMap<String,Topic> loadTopics();

    /**
     * Stores the given topic in RocksDB
     * @param topic topic to store
     */
    void putTopic(Topic topic);

    /**
     * gets topic for the given key
     * @param key  topic key to get Topic from DB
     * @return Topic topic by topic name
     */
    Topic getTopic(byte[] key);

}
