package io.nuls.test.cases.account;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.GetAccountPrivateKeyByAddressReq;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 18:12
 * @Description: 功能描述
 */
@Component
public class GetAccountPriKeyCase extends BaseAccountCase<String,String> {

    @Override
    public String title() {
        return "查询账户私钥";
    }

    @Override
    public String doTest(String param, int depth) throws TestFailException {
        Result<String> prikey = accountService.getAccountPrivateKey(new GetAccountPrivateKeyByAddressReq(PASSWORD,param));
        checkResultStatus(prikey);
        return prikey.getData();
    }
}
