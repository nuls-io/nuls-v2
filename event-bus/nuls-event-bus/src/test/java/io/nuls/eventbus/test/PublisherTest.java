package io.nuls.eventbus.test;

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class PublisherTest {

    @Test
    public void sendEvent(){
        Map<String,Object> params = new HashMap<>();
        params.put("data","This is test event data");
        params.put("topic","ac_create");
        try {
           Response response = CmdDispatcher.requestAndResponse(ModuleE.EB.abbr,"eb_send",params);
            System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
