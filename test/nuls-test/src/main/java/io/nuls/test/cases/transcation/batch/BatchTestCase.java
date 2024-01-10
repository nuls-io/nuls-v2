package io.nuls.test.cases.transcation.batch;

import io.nuls.test.Config;
import io.nuls.test.cases.SleepAdapter;
import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-24 17:54
 * @Description: Function Description
 */
@Component
@TestCase("batchTransfer")
public class BatchTestCase extends TestCaseChain {

    @Autowired
    Config config;

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                BatchReadyNodeAccountCase.class
        };
    }

    @Override
    public String title() {
        return "Local debugging batch creation of transactions";
    }

    @Override
    public Object initParam() {
        int batchTxTotal = (int) config.getBatchTxTotal().longValue();
        return batchTxTotal;
    }
}
