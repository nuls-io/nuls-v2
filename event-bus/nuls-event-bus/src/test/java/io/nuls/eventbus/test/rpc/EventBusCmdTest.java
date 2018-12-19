package io.nuls.eventbus.test.rpc;

import io.nuls.eventbus.EventBus;
import io.nuls.eventbus.model.Topic;
import io.nuls.eventbus.rpc.cmd.EventBusCmd;
import io.nuls.eventbus.rpc.processor.ClientSyncProcessor;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EventBusCmdTest {

    EventBusCmd eventBusCmd = new EventBusCmd();

    @Before
    public void before(){
        ConcurrentMap<String, Topic> topicMap = new ConcurrentHashMap<>();
        Topic topic = new Topic("ac_create",ModuleE.AC.abbr,"Account","io.nuls");
        topicMap.put("ac_create",topic);
        EventBus.getInstance().setTopicMap(topicMap);
        Constants.THREAD_POOL.execute(new ClientSyncProcessor());
    }

    @Test
    public void subscribeTest(){
        Map<String,Object> params = new HashMap<>();
        params.put("topic","ac_create");
        params.put("abbr", ModuleE.LG.abbr);
        try {
            Response response = (Response) eventBusCmd.subscribe(params);
            System.out.println("Response:"+response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
