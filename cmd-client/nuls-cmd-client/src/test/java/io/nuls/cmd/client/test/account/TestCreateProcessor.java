package io.nuls.cmd.client.test.account;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.account.AccountService;
import io.nuls.api.provider.account.facade.CreateAccountReq;
import io.nuls.cmd.client.CmdClientBootstrap;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.account.CreateProcessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 16:23
 * @Description: 功能描述
 */
public class TestCreateProcessor {

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Before
    public void before(){
        CmdClientBootstrap.main(new String[]{});
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreate(){
        CreateAccountReq req = new CreateAccountReq();
        Result<String> res = accountService.createAccount(req);
        Assert.assertTrue(res.isSuccess());
        Assert.assertTrue(res.getList().size() == 1);
        res.getList().forEach(System.out::println);
    }

    @Test public void testCreateForCmd(){
        CreateProcessor cp = new CreateProcessor();
        CommandResult res = cp.execute(new String[0]);
        Assert.assertTrue(res.isSuccess());
        System.out.println(res.getMessage());
    }


}
