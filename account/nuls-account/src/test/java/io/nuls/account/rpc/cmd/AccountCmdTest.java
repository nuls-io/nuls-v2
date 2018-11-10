package io.nuls.account.rpc.cmd;

import io.nuls.account.model.dto.AccountOfflineDto;
import io.nuls.account.model.dto.SimpleAccountDto;
import io.nuls.rpc.cmd.CmdDispatcher;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.parse.JSONUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/2
 */
public class AccountCmdTest {

    protected short chainId = 12345;
    protected String password = "a12345678";
    protected double version = 1.0;

    @BeforeClass
    public static void start() throws Exception {
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

    @Ignore
    @Test
    public void createOfflineAccountTest() throws Exception {
        int count = 10;
        String response = CmdDispatcher.call("ac_createOfflineAccount", new Object[]{chainId, count, password}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        List<AccountOfflineDto> accoutList = JSONUtils.json2list(JSONUtils.obj2json(JSONUtils.json2map(JSONUtils.obj2json(cmdResp.getResult())).get("list")), AccountOfflineDto.class);
        assertEquals(accoutList.size(), count);
        for (AccountOfflineDto account : accoutList) {
            System.out.println(account.getAddress());
        }
    }

    @Ignore
    @Test
    public void getAccountByAddressTest() throws Exception {
        List<String> accoutList = createAccount(chainId, 1, password);
        String response = CmdDispatcher.call("ac_getAccountByAddress", new Object[]{chainId, accoutList.get(0)}, version);
        CmdResponse cmdResp = JSONUtils.json2pojo(response, CmdResponse.class);
        SimpleAccountDto accountDto = JSONUtils.json2pojo(JSONUtils.obj2json(cmdResp.getResult()), SimpleAccountDto.class);
        assertEquals(accoutList.get(0), accountDto.getAddress());
    }

}
