package io.nuls.test.cases.account;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.AccountInfo;
import io.nuls.base.api.provider.account.facade.RemoveAccountReq;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 19:09
 * @Description: 功能描述
 */
@Component
public class GetAccountListCase extends BaseAccountCase<String,String> {

    @Override
    public String title() {
        return "获取账户列表";
    }

    @Override
    public String doTest(String address, int depth) throws TestFailException {
        Result<AccountInfo> list = accountService.getAccountList();
        checkResultStatus(list);
        check(list.getList().stream().anyMatch(d->d.getAddress().equals(address)),"未匹配到创建的地址");
        accountService.removeAccount(new RemoveAccountReq(PASSWORD,address));
        return address;
    }
}
