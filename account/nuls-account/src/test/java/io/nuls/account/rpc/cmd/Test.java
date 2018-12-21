package io.nuls.account.rpc.cmd;

import io.nuls.account.ServiceInitializer;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.service.AccountService;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.ioc.SpringLiteContext;
import org.junit.BeforeClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/2
 */
public class Test {

    //protected static AccountService accountService;

    protected int chainId = 12345;
    protected String password = "a12345678";
    protected String newPassword = "c12345678";
    protected double version2 = 1.0;
    protected String version = "1.0";

    static AccountService accountService;



    @org.junit.Test
    public void setAliasTest() throws Exception {
       String dd = "";
    }


}
