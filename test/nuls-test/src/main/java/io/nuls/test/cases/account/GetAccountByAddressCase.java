package io.nuls.test.cases.account;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.AccountInfo;
import io.nuls.base.api.provider.account.facade.GetAccountByAddressReq;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 10:34
 * @Description: Function Description
 */
@Component
public class GetAccountByAddressCase extends BaseAccountCase<AccountInfo,String> {

    @Override
    public String title() {
        return "Account inquiry through address";
    }

    @Override
    public AccountInfo doTest(String address,int depth) throws TestFailException {
        Result<AccountInfo> result = accountService.getAccountByAddress(new GetAccountByAddressReq(address));
        checkResultStatus(result);
        if(result.getData() == null){
            throw new TestFailException("The result returned by querying the account through the address does not meet expectations,dataEmpty");
        }
        check(result.getData().getAddress().equals(address),"Inconsistent data");
        return result.getData();
    }
}
