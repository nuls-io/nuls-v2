package io.nuls.test.cases.transcation;

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
public class GetTranscationToAddressCase implements TestCaseIntf<String, Transaction> {

    @Override
    public String title() {
        return "从交易对象中提取出金地址";
    }

    @Override
    public String doTest(Transaction param, int depth) throws TestFailException {
        try {
            String toAddress = HexUtil.encode(param.getCoinDataInstance().getTo().get(0).getAddress());
            return toAddress;
        } catch (NulsException e) {
            throw new TestFailException("交易对象中获取coinData失败");
        }
    }
}
