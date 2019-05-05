package io.nuls.test.cases.account;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.AccountInfo;
import io.nuls.base.api.provider.account.facade.GetAccountByAddressReq;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 10:34
 * @Description: 功能描述
 */
@Component
public class GetAccountByAddressCase extends BaseAccountCase<AccountInfo,String> {

    @Override
    public String title() {
        return "通过地址查询账户";
    }

    @Override
    public AccountInfo doTest(String address,int depth) throws TestFailException {
        Result<AccountInfo> result = accountService.getAccountByAddress(new GetAccountByAddressReq(address));
        checkResultStatus(result);
        if(result.getData() == null){
            throw new TestFailException("通过地址查询账户返回结果不符合预期，data为空");
        }
        check(result.getData().getAddress().equals(address),"数据不一致");
        return result.getData();
    }
}
