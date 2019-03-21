package io.nuls.test.cases.account;

import io.nuls.api.provider.account.facade.ImportAccountByKeyStoreReq;
import io.nuls.api.provider.account.facade.UpdatePasswordReq;
import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.tools.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 10:30
 * @Description: 功能描述
 */
@TestCase
@Component
public class AccountCase extends TestCaseChain {

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                CreateAccountCase.class,
                GetAccountByAddressCase.class,
                BackupAccountCase.class,
                RemoveAccountCase.class,
                ImportAccountKeystoreCase.class,
                GetAccountPriKeyCase.class,
                ImportAccountByPriKeyCase.class,
                UpdatePasswordCase.class,
                GetAccountListCase.class
        };
    }

    @Override
    public String title() {
        return "账户模块集成测试";
    }

}
