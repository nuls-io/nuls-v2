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
 * 账户余额一致性测试
 * 1.查询本地余额
 * 2.通过地址查询私钥
 * 3.远程通过私钥查询余额
 * 4.比对本地与远程一致性
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
        return "网络节点余额一致性";
    }

    @Override
    public String doTest(String address, int depth) throws TestFailException {
        AccountInfo accountInfo = accountService.getAccountByAddress(new GetAccountByAddressReq(address)).getData();
        String priKey = accountService.getAccountPrivateKey(new GetAccountPrivateKeyByAddressReq(Constants.PASSWORD,address)).getData();
        new SyncRemoteTestCase<String>(){

            @Override
            public String title() {
                return "远程节点通过私钥导入账户";
            }
        }.check(new RemoteTestParam<>(ImportAccountByPriKeyCase.class,address,priKey),depth);
        boolean res = new SyncRemoteTestCase<AccountInfo>(){

            @Override
            public String title() {
                return "远程比对账户信息一致性";
            }
        }.check(new RemoteTestParam<>(GetAccountByAddressCase.class,accountInfo,address),depth);
        if(!res){
            throw new TestFailException("远程账户信息与本地不一致");
        }
        return address;
    }
}
