package io.nuls.account;

import io.nuls.rpc.cmd.CmdDispatcher;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/2
 */
public class AccountCmdTest {

    protected short chainId = 12345;

    @Test
    public void start() throws Exception {
        CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
    }

    @Ignore
    @Test
    public void createAccountTest() throws Exception {
        System.out.println(CmdDispatcher.call("ac_createAccount", new Object[]{chainId, 1, "a12345678"}, 1.0));
    }

    //@Ignore
    @Test
    public void getAccountByAddressTest() throws Exception {
        System.out.println(CmdDispatcher.call("ac_getAccountByAddress", new Object[]{chainId, "HK1FRBTowpgFPNWr8s6V2RWPtRDGb3930"}, 1.0));
    }

}
