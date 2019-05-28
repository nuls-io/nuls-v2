package io.nuls.test.cases.transcation.batch;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.test.Config;
import io.nuls.test.cases.SleepAdapter;
import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-24 17:54
 * @Description: 功能描述
 */
@Component
@TestCase("debugBatchTransfer")
public class DebugBatchTestCase extends TestCaseChain {



    @Autowired
    Config config;

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                BatchCreateAccountCase.class,
                SleepAdapter.$15SEC.class,
                BatchCreateTransferCase.class,
                SleepAdapter.MAX.class
        };
    }

    @Override
    public String title() {
        return "本地调试批量创建交易";
    }

    @Override
    public Object initParam() {
        BatchParam param = new BatchParam();
        param.count = 5000L;
        param.formAddressPriKey = config.getTestSeedAccount();
        return param;
    }
}
