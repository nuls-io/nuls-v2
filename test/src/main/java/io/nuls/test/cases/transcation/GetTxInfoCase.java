package io.nuls.test.cases.transcation;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.transaction.facade.GetTxByHashReq;
import io.nuls.base.constant.TxStatusEnum;
import io.nuls.base.data.Transaction;
import io.nuls.test.cases.RemoteTestParam;
import io.nuls.test.cases.SyncRemoteTestCase;
import io.nuls.test.cases.TestFailException;
import io.nuls.tools.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 10:46
 * @Description: 功能描述
 */
@Component
public class GetTxInfoCase extends BaseTranscationCase<Transaction,String> {

    @Override
    public String title() {
        return "通过hash获取交易信息";
    }

    @Override
    public Transaction doTest(String param, int depth) throws TestFailException {
        Result<Transaction> result = transferService.getTxByHash(new GetTxByHashReq(param));
        checkResultStatus(result);
        check(result.getData().getStatus().equals(TxStatusEnum.CONFIRMED),"确认状态不符合预期");
        new SyncRemoteTestCase<Transaction>(){

            @Override
            public String title() {
                return "远程交易状态一致性";
            }
        }.check(new RemoteTestParam<>(GetTxInfoCase.class,result.getData(),param),depth);
        return result.getData();
    }
}
