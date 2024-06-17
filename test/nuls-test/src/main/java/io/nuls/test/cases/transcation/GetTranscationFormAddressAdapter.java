package io.nuls.test.cases.transcation;

import io.nuls.base.api.provider.transaction.facade.TransactionData;
import io.nuls.test.cases.BaseAdapter;
import io.nuls.test.cases.CaseType;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 11:59
 * @Description: Function Description
 */
@Component
public class GetTranscationFormAddressAdapter extends BaseAdapter<String, TransactionData> {

    @Override
    public String title() {
        return "Extract the gold address from the trading partner";
    }

    @Override
    public String doTest(TransactionData param, int depth) throws TestFailException {
            return param.getFrom().get(0).getAddress();
    }

    @Override
    public CaseType caseType() {
        return CaseType.Adapter;
    }
}
