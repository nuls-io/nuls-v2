package io.nuls.eventbus.test.rpc.cmd;

import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;

import java.util.Map;

public class EventDataReceiveCmd extends BaseCmd {

    @CmdAnnotation(cmd = "receiveEvent", version = 1.0, description = "Event receive")
    public void receiveEvent(Map<String,Object> params){
        System.out.println("Call back is called");
        String data = (String)params.get("data");
        System.out.println("Received event: "+data);
    }
}
