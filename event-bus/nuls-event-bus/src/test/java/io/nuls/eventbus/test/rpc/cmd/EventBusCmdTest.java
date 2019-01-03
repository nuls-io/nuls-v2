package io.nuls.eventbus.test.rpc.cmd;

import io.nuls.db.service.RocksDBService;
import io.nuls.eventbus.EventBus;
import io.nuls.eventbus.constant.EbConstants;
import io.nuls.eventbus.model.Topic;
import io.nuls.eventbus.rpc.cmd.EventBusCmd;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EventBusCmdTest {

    private final EventBusCmd eventBusCmd = new EventBusCmd();

    @BeforeClass
    public static void set() throws Exception {
        SpringLiteContext.init(EbConstants.EB_BASE_PACKAGE, new ModularServiceMethodInterceptor());
    }

    @Before
    public void before(){
        EbConstants.MODULE_CONFIG_MAP.put(EbConstants.ENCODING,"UTF-8");
        RocksDBService.init("../../eventbus");
        if(!RocksDBService.existTable(EbConstants.TB_EB_TOPIC)){
            try {
                RocksDBService.createTable(EbConstants.TB_EB_TOPIC);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
            System.out.println("Response:"+response);
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
            System.out.println("Response:"+response);
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
