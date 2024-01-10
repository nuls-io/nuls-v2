package io.nuls.test.cases.account;

import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.account.facade.AccountInfo;
import io.nuls.base.api.provider.account.facade.GetAccountByAddressReq;
import io.nuls.base.api.provider.account.facade.GetAccountPrivateKeyByAddressReq;
import io.nuls.base.api.provider.ledger.LedgerProvider;
import io.nuls.test.Config;
import io.nuls.test.cases.*;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

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
public class SyncAccountInfo extends BaseAccountCase<String, String> {

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
        AccountInfo accountInfo = accountService.getAccountByAddress(new GetAccountByAddressReq(address)).getData();
        String priKey = accountService.getAccountPrivateKey(new GetAccountPrivateKeyByAddressReq(Constants.PASSWORD,address)).getData();
        new SyncRemoteTestCase<String>(){

            @Override
            public String title() {
                return "Remote nodes import accounts through private keys";
            }
        }.check(new RemoteTestParam<>(ImportAccountByPriKeyCase.class,address,priKey),depth);
        boolean res = new SyncRemoteTestCase<AccountInfo>(){

            @Override
            public String title() {
                return "Remote comparison of account information consistency";
            }
        }.check(new RemoteTestParam<>(GetAccountByAddressCase.class,accountInfo,address),depth);
        if(!res){
            throw new TestFailException("Remote account information is inconsistent with local information");
        }
        return address;
    }
}
