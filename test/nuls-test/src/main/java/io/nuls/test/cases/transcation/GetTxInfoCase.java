package io.nuls.test.cases.transcation;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.transaction.facade.GetConfirmedTxByHashReq;
import io.nuls.base.api.provider.transaction.facade.TransactionData;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 10:46
 * @Description: Function Description
 */
@Component
public class GetTxInfoCase extends BaseTranscationCase<TransactionData,String> {

    @Override
    public String title() {
        return "adopthashObtain transaction information";
    }

    @Override
    public TransactionData doTest(String param, int depth) throws TestFailException {
        Result<TransactionData> result = transferService.getSimpleTxDataByHash(new GetConfirmedTxByHashReq(param));
        checkResultStatus(result);
        check(result.getData().getStatus().equals(TxStatusEnum.CONFIRMED),"Transaction status does not meet expectations, unconfirmed");
        return result.getData();
    }
}
