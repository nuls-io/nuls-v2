package io.nuls.test.cases.account;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.CreateAccountReq;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 10:25
 * @Description: 功能描述
 */
@Component
public class CreateAccountCase extends BaseAccountCase<String,Void> {

    @Override
    public String title() {
        return "创建账户";
    }

    @Override
    public String doTest(Void param,int depth) throws TestFailException {
        Result<String> result = accountService.createAccount(new CreateAccountReq(1, Constants.PASSWORD));
        checkResultStatus(result);
        if(result.getList() == null || result.getList().isEmpty()){
            throw new TestFailException("创建账户返回结果不符合预期，list为空");
        }
        return result.getList().get(0);
    }
}
