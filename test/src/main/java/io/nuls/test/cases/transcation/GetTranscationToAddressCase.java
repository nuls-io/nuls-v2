package io.nuls.test.cases.transcation;

import io.nuls.api.provider.transaction.facade.TransactionData;
import io.nuls.base.data.Transaction;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.test.cases.TestFailException;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 11:59
 * @Description: 功能描述
 */
@Component
public class GetTranscationToAddressCase implements TestCaseIntf<String, TransactionData> {

    @Override
    public String title() {
        return "从交易对象中提取出金地址";
    }

    @Override
    public String doTest(TransactionData param, int depth) throws TestFailException {
            return param.getTo().get(0).getAddress();
    }
}
