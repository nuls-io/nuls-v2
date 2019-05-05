package io.nuls.test.cases.transcation;

import io.nuls.base.api.provider.transaction.facade.TransactionData;
import io.nuls.test.cases.BaseAdapter;
import io.nuls.test.cases.CaseType;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 11:59
 * @Description: 功能描述
 */
@Component
public class GetTranscationToAddressAdapter extends BaseAdapter<String, TransactionData> {

    @Override
    public String title() {
        return "从交易对象中提取入金地址";
    }

    @Override
    public String doTest(TransactionData param, int depth) throws TestFailException {
            return param.getTo().get(0).getAddress();
    }

    @Override
    public CaseType caseType() {
        return CaseType.Adapter;
    }
}
