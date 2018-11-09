package io.nuls.account.rpc.cmd;

import io.nuls.account.model.bo.Account;
import io.nuls.account.model.dto.SimpleAccountDto;
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
    protected String password = "a12345678";
    protected double version = 1.0;

    @Test
    public void start() throws Exception {
        CmdDispatcher.syncKernel("ws://127.0.0.1:8887");
    }

    private List<String> createAccount(short chainId, int count, String password) throws Exception {
        String response = CmdDispatcher.call("ac_createAccount", new Object[]{chainId, count, password}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        List<String> accoutList = (List<String>) JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list");
        return accoutList;
    }

    @Ignore
    @Test
    public void createAccountTest() throws Exception {
        int count = 10;
        List<String> accoutList = createAccount(chainId, count, password);
        assertEquals(accoutList.size(), count);
        for (String address : accoutList) {
            System.out.println(address);
        }
    }

    //@Ignore
    @Test
    public void getAccountByAddressTest() throws Exception {
        List<String> accoutList = createAccount(chainId, 1, password);
        String response = CmdDispatcher.call("ac_getAccountByAddress", new Object[]{chainId, accoutList.get(0)}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        SimpleAccountDto accountDto = JSONUtils.json2pojo(JSONUtils.obj2json(cmdResp.getResult()), SimpleAccountDto.class);
        assertEquals(accoutList.get(0), accountDto.getAddress());
    }

}
