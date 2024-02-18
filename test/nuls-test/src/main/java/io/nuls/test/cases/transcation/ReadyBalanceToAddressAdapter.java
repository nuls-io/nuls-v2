package io.nuls.test.cases.transcation;

import io.nuls.test.cases.CaseType;
import io.nuls.test.cases.SleepAdapter;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-22 11:52
 * @Description: Function Description
 */
@Component
public class ReadyBalanceToAddressAdapter extends TestCaseChain {

    @Override
    public CaseType caseType(){
        return CaseType.Adapter;
    }

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                TransferToAddressCase.class,
                SleepAdapter.$30SEC.class,
                SyncTxInfoCase.class,
                GetTranscationToAddressAdapter.class
        };
    }

    @Override
    public String title() {
        return "Prepare balance for the specified address";
    }
}
