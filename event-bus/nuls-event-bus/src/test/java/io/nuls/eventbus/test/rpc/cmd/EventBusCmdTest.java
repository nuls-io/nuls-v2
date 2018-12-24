package io.nuls.eventbus.test.rpc.cmd;

import io.nuls.eventbus.EventBus;
import io.nuls.eventbus.model.Topic;
import io.nuls.eventbus.rpc.cmd.EventBusCmd;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EventBusCmdTest {

    private final EventBusCmd eventBusCmd = new EventBusCmd();

    @Before
    public void before(){
        ConcurrentMap<String, Topic> topicMap = new ConcurrentHashMap<>();
        Topic topic = new Topic("ac_create",ModuleE.AC.abbr,"Account","io.nuls");
        topicMap.put("ac_create",topic);
        EventBus.getInstance().setTopicMap(topicMap);
    }

    @Test
    public void subscribe(){
        Map<String,Object> params = new HashMap<>();
        params.put("topic","ac_create");
        params.put("role", ModuleE.LG.abbr);
        params.put("callBackCmd", "eventReceive");
        try {
            Response response = (Response) eventBusCmd.subscribe(params);
            Assert.assertEquals("1",response.getResponseStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void subscribeParamMissing(){
        Map<String,Object> params = new HashMap<>();
        params.put("topic","ac_create");
        params.put("role", ModuleE.LG.abbr);
        try {
            Response response = (Response) eventBusCmd.subscribe(params);
            Assert.assertEquals("0",response.getResponseStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void subscribeTopicNotFound(){
        Map<String,Object> params = new HashMap<>();
        params.put("topic","demo_topic");
        params.put("role", ModuleE.LG.abbr);
        try {
            Response response = (Response) eventBusCmd.subscribe(params);
            Assert.assertEquals("0",response.getResponseStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void unsubscribe(){
        Map<String,Object> params = new HashMap<>();
        params.put("topic","ac_create");
        params.put("role", ModuleE.LG.abbr);
        try {
            Response response = (Response) eventBusCmd.unsubscribe(params);
            Assert.assertEquals("1",response.getResponseStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void unsubscribeTopicNotFound(){
        Map<String,Object> params = new HashMap<>();
        params.put("topic","demo_topic");
        params.put("role", ModuleE.LG.abbr);
        try {
            Response response = (Response) eventBusCmd.unsubscribe(params);
            Assert.assertEquals("0",response.getResponseStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sendParamMissing(){
        Map<String,Object> params = new HashMap<>();
        params.put("role", ModuleE.LG.abbr);
        try {
            Response response = (Response) eventBusCmd.send(params);
            Assert.assertEquals("0",response.getResponseStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
