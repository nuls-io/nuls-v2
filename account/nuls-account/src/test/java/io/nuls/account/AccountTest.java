package io.nuls.account;

import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.info.RuntimeInfo;
import io.nuls.rpc.server.WsServer;
import io.nuls.tools.parse.JSONUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/2
 */
public class AccountTest {

    @Test
    public void start() throws Exception {
        CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
        //createAccountTest();
        //Thread.sleep(Integer.MAX_VALUE);
    }

    @Ignore
    @Test
    public void createAccountTest() throws Exception {
        System.out.println(CmdDispatcher.call("ac_createAccount", new Object[]{1234, 1, "a12345678"}, 1.0));
    }

    //@Ignore
    @Test
    public void getAccountByAddressTest() throws Exception {
        System.out.println(CmdDispatcher.call("ac_getAccountByAddress", new Object[]{1234, "QdDDUGGeBqEzXvkxuZ7v9EDeYjsbT-Gyy"}, 1.0));
        System.out.println(CmdDispatcher.call("ac_getAccountByAddress", new Object[]{1234, "XPbaKXTYXSD98ejaT6RcLLW9Dbumk-Gyy"}, 1.0));
    }

}
