package io.nuls.test.cases.transcation;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.account.AccountService;
import io.nuls.api.provider.account.facade.SetAccountAliasReq;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.test.cases.TestFailException;
import io.nuls.tools.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 14:38
 * @Description: 功能描述
 */
@Component
public class SetAliasCase implements TestCaseIntf<String,String> {

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
