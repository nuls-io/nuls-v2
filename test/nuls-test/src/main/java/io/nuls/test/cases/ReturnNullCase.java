package io.nuls.test.cases;

import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-10 15:29
 * @Description: 功能描述
 */
@Component
public class ReturnNullCase extends BaseTestCase<String,String> {

    @Override
    public String title() {
        return "remote connect test";
    }

    @Override
    public String doTest(String param, int depth) throws TestFailException {
        return "result:" + param;
    }

    @Override
    public CaseType caseType() {
        return CaseType.Test;
    }
}
