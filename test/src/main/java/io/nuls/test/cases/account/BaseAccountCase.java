package io.nuls.test.cases.account;

import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.account.AccountService;
import io.nuls.test.cases.BaseTestCase;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.TestCaseIntf;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 10:35
 * @Description: 功能描述
 */
public abstract class BaseAccountCase<T,P> extends BaseTestCase<T,P> {

    AccountService accountService = ServiceManager.get(AccountService.class);

    public static final String PASSWORD = Constants.PASSWORD;


}
