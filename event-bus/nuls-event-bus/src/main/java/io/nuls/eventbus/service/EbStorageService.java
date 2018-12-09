package io.nuls.eventbus.service;

import io.nuls.eventbus.model.Topic;

public interface EbStorageService {

    void init();

    void loadTopics();

    void putTopic(Topic topic);

    Topic getTopic(byte[] key);

}
