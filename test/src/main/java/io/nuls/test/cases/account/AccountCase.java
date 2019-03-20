package io.nuls.test.cases.account;

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
        return new Class[]{CreateAccountCase.class,GetAccountByAddressCase.class};
    }

    @Override
    public String title() {
        return "账户模块集成测试";
    }

}
