package io.nuls.test.cases.account;

import io.nuls.base.api.provider.account.facade.GetAccountByAddressReq;
import io.nuls.base.api.provider.account.facade.RemoveAccountReq;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 18:02
 * @Description: Function Description
 */
@Component
public class RemoveAccountCase extends BaseAccountCase<String,String> {

    @Override
    public String title() {
        return "Delete account";
    }

    @Override
    public String doTest(String param, int depth) throws TestFailException {
        checkResultStatus(accountService.removeAccount(new RemoveAccountReq(PASSWORD,param)));
        check(accountService.getAccountByAddress(new GetAccountByAddressReq(param)).getData() == null,"Account deletion failed");
        return param;
    }
}
