package io.nuls.poc.rpc;

import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.core.annotation.Component;

/**
 * 共识RPC类
 * @author tag
 * 2018/11/7
 * */
@Component
public class PocConsensusResource extends BaseCmd{

    @CmdAnnotation(cmd = "cs_createAgent", version = 1.0, preCompatible = true)
    public CmdResponse createAgent(){
        System.out.println("I'm version 1");
        return success(1.0, "cmd1->1.0", null);
    }
}
