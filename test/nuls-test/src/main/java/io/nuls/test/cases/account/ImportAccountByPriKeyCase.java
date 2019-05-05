package io.nuls.test.cases.account;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.ImportAccountByPrivateKeyReq;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 18:11
 * @Description: 功能描述
 */
@Component
public class ImportAccountByPriKeyCase extends BaseAccountCase<String,String> {

    @Override
    public String title() {
        return "通过私钥导入账户";
    }

    @Override
    public String doTest(String prikey, int depth) throws TestFailException {
        Result<String> result = accountService.importAccountByPrivateKey(new ImportAccountByPrivateKeyReq(PASSWORD,prikey,true));
        checkResultStatus(result);
        return result.getData();
    }
}
