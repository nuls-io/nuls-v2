package io.nuls.account;

import io.nuls.account.model.bo.Account;
import io.nuls.account.model.po.AccountPo;
import io.nuls.account.util.AccountTool;
import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.parse.JSONUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    //@Ignore
    @Test
    public void createAccountTest() throws Exception {
        int count = 10;
        String password = "a12345678";
        String response = CmdDispatcher.call("ac_createAccount", new Object[]{chainId, count, password}, 1.0);
        System.out.println(response);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        List<String> accoutList = (List<String>) JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list");
        assertEquals(accoutList.size(), count);
        for (String address : accoutList) {
            System.out.println(address);
        }
    }

    @Ignore
    @Test
    public void getAccountByAddressTest() throws Exception {
        System.out.println(CmdDispatcher.call("ac_getAccountByAddress", new Object[]{chainId, "HK1FRBTowpgFPNWr8s6V2RWPtRDGb3930"}, 1.0));
    }

}
