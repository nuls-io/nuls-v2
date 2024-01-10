package io.nuls.test.cases;

import io.nuls.core.core.ioc.SpringLiteContext;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 10:18
 * @Description: Test case chain
 */
public abstract class TestCaseChain extends BaseTestCase<Object,Object> {

    public abstract Class<? extends TestCaseIntf>[] testChain();

    @Override
    public Object doTest(Object param,int depth) throws TestFailException {
        Class<? extends TestCaseIntf>[] testCases = this.testChain();
        for (Class tc: testCases) {

            TestCaseIntf t = (TestCaseIntf) SpringLiteContext.getBean(tc);
            param = t.check(param,depth);
        }
        return param;
    }

}
