package io.nuls.test.cases.account;

import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.account.AccountService;
import io.nuls.test.Config;
import io.nuls.test.cases.BaseTestCase;
import io.nuls.test.cases.Constants;
import io.nuls.core.core.annotation.Autowired;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 10:35
 * @Description: 功能描述
 */
public abstract class BaseAccountCase<T,P> extends BaseTestCase<T,P> {

    protected AccountService accountService = ServiceManager.get(AccountService.class);

    public static final String PASSWORD = Constants.PASSWORD;

    @Autowired
    protected Config config;

}
