package io.nuls.test.cases.account;

import io.nuls.base.api.provider.account.facade.AccountInfo;
import io.nuls.test.cases.BaseAdapter;
import io.nuls.test.cases.CaseType;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-22 09:46
 * @Description: 功能描述
 */
@Component
public class GetAddressByAccountInfoAdapder extends BaseAdapter<String, AccountInfo> {

    @Override
    public String title() {
        return "从account中提取address";
    }

    @Override
    public String doTest(AccountInfo param, int depth) throws TestFailException {
        return param.getAddress();
    }

    @Override
    public CaseType caseType() {
        return CaseType.Adapter;
    }

}
