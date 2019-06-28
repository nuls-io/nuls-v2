package io.nuls.test.cases.transcation;

import io.nuls.core.core.annotation.Component;
import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.test.cases.account.*;

/**
 * @Author: ljs
 * @Time: 2019-03-20 10:30
 * @Description:
 *
 */
@TestCase("ljsTest")
@Component
public class LjsTestCase extends TestCaseChain {

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                BatchFatigueTxsCase.class
        };
    }

    @Override
    public String title() {
        return "我的集成测试";
    }

}
