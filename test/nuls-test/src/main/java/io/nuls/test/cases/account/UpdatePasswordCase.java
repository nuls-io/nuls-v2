package io.nuls.test.cases.account;

import io.nuls.base.api.provider.account.facade.UpdatePasswordReq;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 19:05
 * @Description: 功能描述
 */
@Component
public class UpdatePasswordCase extends BaseAccountCase<String,String> {

    static final String NEW_PWD = "nuls654321";

    @Override
    public String title() {
        return "修改账户密码";
    }

    @Override
    public String doTest(String param, int depth) throws TestFailException {
        checkResultStatus(accountService.updatePassword(new UpdatePasswordReq(param,PASSWORD,NEW_PWD)));
        checkResultStatus(accountService.updatePassword(new UpdatePasswordReq(param,NEW_PWD,PASSWORD)));
        return param;
    }
}
