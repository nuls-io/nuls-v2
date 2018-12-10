package io.nuls.eventbus.service;

import io.nuls.db.service.RocksDBService;
import io.nuls.eventbus.EventBus;
import io.nuls.eventbus.constant.EBConstants;
import io.nuls.eventbus.model.Topic;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.ObjectUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.log.Log;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class EbStorageServiceImpl implements EbStorageService {

    @Override
    public void init() {
        try{
            RocksDBService.init("../../eventbus");
            if(!RocksDBService.existTable(EBConstants.TB_EB_TOPIC)){
                RocksDBService.createTable(EBConstants.TB_EB_TOPIC);
            }
        }catch (Exception e){
            Log.error("RocksDb init failed");
        }
    }

    @Override
    public void loadTopics() {
        try{
            ConcurrentMap<String,Topic> topicMap = new ConcurrentHashMap();
            List<byte[]> keys = RocksDBService.keyList(EBConstants.TB_EB_TOPIC);
            if(!keys.isEmpty()){
                Map<byte[],byte[]> map = RocksDBService.multiGet(EBConstants.TB_EB_TOPIC,keys);
                for(byte[] key : map.keySet()){
                    byte[] obj = map.get(key);
                    topicMap.put(ObjectUtils.bytesToObject(key),ObjectUtils.bytesToObject(obj));
                }
                EventBus.getInstance().setTopicMap(topicMap);
            }
        }catch (Exception e){
           Log.error("Error while loading Topics from DB");
        }

    }

    @Override
    public void putTopic(Topic topic) {
        try{
            if(null != topic && StringUtils.isNotBlank(topic.getTopicId())){
                RocksDBService.put(EBConstants.TB_EB_TOPIC,topic.getTopicId().getBytes(),ObjectUtils.objectToBytes(topic));
            }
        }catch (Exception e){
            Log.error("Topic save failed :"+e.getMessage());
        }
    }

    @Override
    public Topic getTopic(byte[] key) {
        return ObjectUtils.bytesToObject(RocksDBService.get(EBConstants.TB_EB_TOPIC,key));
    }
}
