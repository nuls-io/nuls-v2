package io.nuls.test.cases.ledger;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.ledger.LedgerProvider;
import io.nuls.api.provider.ledger.facade.AccountBalanceInfo;
import io.nuls.api.provider.ledger.facade.GetBalanceReq;
import io.nuls.test.Config;
import io.nuls.test.cases.AbstractRemoteTestCase;
import io.nuls.test.cases.RemoteTestParam;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.cases.account.GetAccountPriKeyCase;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 11:21
 * @Description:
 * 账户余额一致性测试
 * 1.查询本地余额
 * 2.通过地址查询私钥
 * 3.远程通过私钥查询余额
 * 4.比对本地与远程一致性
 */
@Component
public class SyncAccountBalance implements TestCaseIntf<String, String> {

    LedgerProvider ledgerProvider = ServiceManager.get(LedgerProvider.class);

    @Autowired
    GetAccountPriKeyCase getAccountPriKeyCase;

    @Autowired
    Config config;

    @Override
    public String title() {
        return "网络节点余额一致性";
    }

    @Override
    public String doTest(String address, int depth) throws TestFailException {
        Result<AccountBalanceInfo> result = ledgerProvider.getBalance(new GetBalanceReq(config.getAssetsId(),config.getChainId(),address));
        String priKey = getAccountPriKeyCase.check(address,depth);
        boolean res = new AbstractRemoteTestCase<AccountBalanceInfo>(){

            @Override
            public String title() {
                return "远程比对账户余额";
            }
        }.check(new RemoteTestParam<>(GetAccountBalanceByPriKeyCase.class,result.getData(),priKey),depth);
        if(!res){
            throw new TestFailException("远程账户余额与本地不一致");
        }
        return address;
    }
}
