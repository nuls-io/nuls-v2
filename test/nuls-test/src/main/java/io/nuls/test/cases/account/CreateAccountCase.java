package io.nuls.test.cases.account;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.CreateAccountReq;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 10:25
 * @Description: Function Description
 */
@Component
public class CreateAccountCase extends BaseAccountCase<String,Void> {

    @Override
    public String title() {
        return "Create an account";
    }

    @Override
    public String doTest(Void param,int depth) throws TestFailException {
        Result<String> result = accountService.createAccount(new CreateAccountReq(1, Constants.PASSWORD));
        checkResultStatus(result);
        if(result.getList() == null || result.getList().isEmpty()){
            throw new TestFailException("The result of creating an account does not meet expectations,listEmpty");
        }
        return result.getList().get(0);
    }
}
