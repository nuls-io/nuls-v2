package io.nuls.test.cases.transcation;

import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 20:07
 * @Description: Function Description
 */
@TestCase("transaction")
@Component
public class TransactionCase extends TestCaseChain {
    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                TransferCase.class,
                AliasTransferCase.class
        };
    }

    @Override
    public String title() {
        return "Transaction module";
    }
}
