package io.nuls.test.cases.account;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.BackupAccountReq;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 17:25
 * @Description: 功能描述
 */
@Component
public class BackupAccountCase extends BaseAccountCase<String,String> {

    @Override
    public String title() {
        return "备份账户到keystore";
    }

    @Override
    public String doTest(String param, int depth) throws TestFailException {
        Result<String> result = accountService.backupAccount(new BackupAccountReq(PASSWORD,param,System.getProperty("user.dir")));
        checkResultStatus(result);
        return param;
    }
}
