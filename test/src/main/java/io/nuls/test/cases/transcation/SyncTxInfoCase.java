package io.nuls.test.cases.transcation;

import io.nuls.api.provider.transaction.facade.TransactionData;
import io.nuls.base.constant.TxStatusEnum;
import io.nuls.base.data.Transaction;
import io.nuls.test.cases.RemoteTestParam;
import io.nuls.test.cases.SyncRemoteTestCase;
import io.nuls.test.cases.TestFailException;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 15:38
 * @Description: 功能描述
 */
@Component
public class SyncTxInfoCase extends BaseTranscationCase<TransactionData,String> {

    @Autowired GetTxInfoCase getTxInfoCase;

    @Override
    public String title() {
        return "交易信息数据一致性";
    }

    @Override
    public TransactionData doTest(String hash, int depth) throws TestFailException {
        TransactionData transaction = getTxInfoCase.check(hash,depth);
        new SyncRemoteTestCase<TransactionData>(){

            @Override
            public String title() {
                return "远程交易状态一致性";
            }
        }.check(new RemoteTestParam<>(GetTxInfoCase.class,transaction,hash),depth);
        return transaction;
    }
}
