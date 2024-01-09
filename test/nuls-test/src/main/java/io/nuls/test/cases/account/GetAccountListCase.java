package io.nuls.test.cases.account;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.AccountInfo;
import io.nuls.base.api.provider.account.facade.RemoveAccountReq;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 19:09
 * @Description: Function Description
 */
@Component
public class GetAccountListCase extends BaseAccountCase<String,String> {

    @Override
    public String title() {
        return "Get account list";
    }

    @Override
    public String doTest(String address, int depth) throws TestFailException {
        Result<AccountInfo> list = accountService.getAccountList();
        checkResultStatus(list);
        check(list.getList().stream().anyMatch(d->d.getAddress().equals(address)),"Unable to match the created address");
        accountService.removeAccount(new RemoveAccountReq(PASSWORD,address));
        return address;
    }
}
