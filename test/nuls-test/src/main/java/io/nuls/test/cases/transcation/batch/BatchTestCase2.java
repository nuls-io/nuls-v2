package io.nuls.test.cases.transcation.batch;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.test.Config;
import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-24 17:54
 * @Description: Function Description
 */
@Component
public class BatchTestCase2 extends TestCaseChain {

    @Autowired
    Config config;

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                BatchReadyNodeAccountCase2.class
        };
    }

    @Override
    public String title() {
        return "Local debugging batch creation of transactions";
    }

    @Override
    public Object initParam() {
        return 10000;
    }
}
