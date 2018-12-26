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

public class SubscriberTest {

    @Before
    public void before(){
        try {
            WsServer.getInstance(ModuleE.LG.abbr, "module", "test.com")
                    .moduleRoles(ModuleE.LG.abbr, new String[]{"1.0"})
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
    public void subscribe(){
        Map<String,Object> params = new HashMap<>();
        params.put("role",ModuleE.LG);
        params.put("topic","ac_create");
        params.put("callBackCmd","receiveEvent");
        try {
            Response response = CmdDispatcher.requestAndResponse(ModuleE.EB.abbr,"eb_subscribe",params);
            System.out.println(response.toString());
            Assert.assertEquals("1",response.getResponseStatus());
            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
