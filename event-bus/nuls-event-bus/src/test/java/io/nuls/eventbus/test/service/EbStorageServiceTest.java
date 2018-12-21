package io.nuls.eventbus.test.service;

import io.nuls.db.service.RocksDBService;
import io.nuls.eventbus.constant.EbConstants;
import io.nuls.eventbus.model.Subscriber;
import io.nuls.eventbus.model.Topic;
import io.nuls.eventbus.service.EbStorageService;
import io.nuls.eventbus.service.EbStorageServiceImpl;
import io.nuls.rpc.model.ModuleE;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ConcurrentMap;

public class EbStorageServiceTest {

    private final EbStorageService ebStorageService = new EbStorageServiceImpl();

    @Before
    public void before(){
        RocksDBService.init("../../eventbus");
        if(!RocksDBService.existTable(EbConstants.TB_EB_TOPIC)){
            try {
                RocksDBService.createTable(EbConstants.TB_EB_TOPIC);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void storeTopic(){
        Topic topic = new Topic("ac_create", ModuleE.AC.abbr,"account","nuls.io");
        Subscriber subscriber = new Subscriber(ModuleE.LG.abbr,"Ledger","nuls.io");
        Subscriber subscriber1 = new Subscriber(ModuleE.BL.abbr,"block","nuls.io");
        topic.addSubscriber(subscriber);
        topic.addSubscriber(subscriber1);
        ebStorageService.putTopic(topic);

    }

    @Test
    public void getTopic(){
        Topic fromStorage = ebStorageService.getTopic("ac_create".getBytes());
        System.out.println("Topic id:"+fromStorage.getTopicId());
        for(Subscriber sub : fromStorage.getSubscribers()){
            System.out.println("Subscriber module:"+sub.getModuleAbbr());
        }
        Assert.assertTrue("ac_create".equals(fromStorage.getTopicId()));
    }

    @Test
    public void loadAllTopics(){
        ConcurrentMap<String,Topic> topicMap = ebStorageService.loadTopics();
        System.out.println(topicMap.keySet().toString());
        Assert.assertNotNull(topicMap);
    }
}
