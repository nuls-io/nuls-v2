package io.nuls.test.cases.transcation;

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
public class SyncTxInfoCase extends BaseTranscationCase<Transaction,String> {

    @Autowired GetTxInfoCase getTxInfoCase;

    @Override
    public String title() {
        return "交易信息数据一致性";
    }

    @Override
    public Transaction doTest(String hash, int depth) throws TestFailException {
        Transaction transaction = getTxInfoCase.check(hash,depth);
        new SyncRemoteTestCase<Transaction>(){

            @Override
            public String title() {
                return "远程交易状态一致性";
            }
        }.check(new RemoteTestParam<>(GetTxInfoCase.class,transaction,hash),depth);
        return transaction;
    }
}
