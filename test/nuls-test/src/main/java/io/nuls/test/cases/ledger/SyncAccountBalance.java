package io.nuls.test.cases.ledger;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.ledger.LedgerProvider;
import io.nuls.base.api.provider.ledger.facade.AccountBalanceInfo;
import io.nuls.base.api.provider.ledger.facade.GetBalanceReq;
import io.nuls.test.Config;
import io.nuls.test.cases.*;
import io.nuls.test.cases.account.GetAccountPriKeyCase;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 11:21
 * @Description:
 * Account balance consistency test
 * 1.Query local balance
 * 2.Query private key through address
 * 3.Remote balance query through private key
 * 4.Compare local and remote consistency
 */
@Component
public class SyncAccountBalance extends BaseTestCase<String, String> {

    LedgerProvider ledgerProvider = ServiceManager.get(LedgerProvider.class);

    @Autowired
    GetAccountPriKeyCase getAccountPriKeyCase;

    @Autowired
    Config config;

    @Override
    public String title() {
        return "Network node balance consistency";
    }

    @Override
    public String doTest(String address, int depth) throws TestFailException {
        Result<AccountBalanceInfo> result = ledgerProvider.getBalance(new GetBalanceReq(config.getAssetsId(),config.getChainId(),address));
        check(result.getData().getTotal().equals(Constants.TRANSFER_AMOUNT),"The total balance of the receiving asset account does not meet expectations");
        check(result.getData().getAvailable().equals(Constants.TRANSFER_AMOUNT),"The availability of the receiving asset account does not meet expectations");
        check(result.getData().getFreeze().equals(BigInteger.ZERO),"The frozen balance of the receiving asset account does not meet expectations");
        String priKey = getAccountPriKeyCase.check(address,depth);
        boolean res = new SyncRemoteTestCase<AccountBalanceInfo>(){

            @Override
            public String title() {
                return "Remote comparison of account balance";
            }
        }.check(new RemoteTestParam<>(GetAccountBalanceByPriKeyCase.class,result.getData(),priKey),depth);
        if(!res){
            throw new TestFailException("Remote account balance does not match local balance");
        }
        return address;
    }
}
