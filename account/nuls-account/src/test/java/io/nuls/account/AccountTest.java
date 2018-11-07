package io.nuls.account;

import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.parse.JSONUtils;
import org.junit.Test;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/2
 */
public class AccountTest {
    @Test
    public void test() throws Exception {

        CmdDispatcher.syncKernel("ws://127.0.0.1:8887");

        System.out.println(RuntimeInfo.remoteModuleMap.size());
        System.out.println(JSONUtils.obj2json(RuntimeInfo.remoteModuleMap));

        System.out.println(CmdDispatcher.call("ac_createAccount", new Object[]{1234, 1, "a12345678"}, 1.0));

        //Thread.sleep(Integer.MAX_VALUE);
    }
}
