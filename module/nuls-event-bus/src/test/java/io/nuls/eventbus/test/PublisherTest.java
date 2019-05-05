package io.nuls.eventbus.test;

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.server.WsServer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class PublisherTest {

    @Before
    public void before(){
        try {
            WsServer.getInstance(ModuleE.AC.abbr, "module", "test.com")
                    .moduleRoles(ModuleE.AC.abbr, new String[]{"1.0"})
                    .moduleVersion("1.0")
                    .dependencies(ModuleE.EB.abbr, "1.0")
                    .connect("ws://127.0.0.1:8887");

            // Get information from kernel
            CmdDispatcher.syncKernel();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void sendEvent(){
        Map<String,Object> params = new HashMap<>();
        params.put("entity","This is test event entity");
        params.put("topic","ac_create");
        try {
           Response response = CmdDispatcher.requestAndResponse(ModuleE.EB.abbr,"eb_send",params);
            System.out.println(response.toString());
            Assert.assertEquals("1",response.getResponseStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
