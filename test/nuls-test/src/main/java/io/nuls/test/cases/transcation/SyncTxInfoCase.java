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
 * @Description: Function Description
 */
@Component
public class SyncTxInfoCase extends BaseTranscationCase<TransactionData,String> {

    @Autowired GetTxInfoCase getTxInfoCase;

    @Override
    public String title() {
        return "Consistency of transaction information data";
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
                return "Remote transaction status consistency";
            }
        }.check(new RemoteTestParam<>(GetTxInfoCase.class,transaction,hash),depth)){
            throw new TestFailException("Remote transaction status locally inconsistent");
        }
        return transaction;
    }
}
