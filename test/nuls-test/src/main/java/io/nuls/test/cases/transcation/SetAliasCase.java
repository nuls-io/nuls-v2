package io.nuls.test.cases.transcation;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.account.AccountService;
import io.nuls.base.api.provider.account.facade.SetAccountAliasReq;
import io.nuls.test.cases.BaseTestCase;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 14:38
 * @Description: 功能描述
 */
@Component
public class SetAliasCase extends BaseTestCase<String,String> {

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Override
    public String title() {
        return "设置别名";
    }

    @Override
    public String doTest(String param, int depth) throws TestFailException {
        Result<String> result = accountService.setAccountAlias(new SetAccountAliasReq(Constants.PASSWORD,param,Constants.getAlias(param)));
        checkResultStatus(result);
        return result.getData();
    }
}
