package io.nuls.test.cases.transcation;

import io.nuls.base.api.provider.transaction.facade.TransactionData;
import io.nuls.test.cases.RemoteTestParam;
import io.nuls.test.cases.SyncRemoteTestCase;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

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
        if(!new SyncRemoteTestCase<TransactionData>(){

            @Override
            public boolean equals(TransactionData source, TransactionData remote){
                source.setTime(null);
                remote.setTime(null);
                return source.equals(remote);
            }

            @Override
            public String title() {
                return "远程交易状态一致性";
            }
        }.check(new RemoteTestParam<>(GetTxInfoCase.class,transaction,hash),depth)){
            throw new TestFailException("远程交易状态本地不一致");
        }
        return transaction;
    }
}
