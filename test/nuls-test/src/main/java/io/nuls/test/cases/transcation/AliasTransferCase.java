package io.nuls.test.cases.transcation;

import io.nuls.test.cases.SleepAdapter;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.test.cases.account.SyncAccountInfo;
import io.nuls.test.cases.ledger.SyncAccountBalance;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 14:36
 * @Description:
 * Set alias test
 * 1.Set alias
 * 2.wait for10second
 * 3.Check if the alias transaction has been confirmed
 * 4.Check if the local alias has been successfully set
 * 5.Check if the account information of network nodes is consistent
 */
@Component
public class AliasTransferCase extends TestCaseChain {

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                SetAliasCase.class,
                SleepAdapter.$30SEC.class,
                SyncTxInfoCase.class,
                GetTranscationFormAddressAdapter.class,
                CheckAliasCase.class,
                SyncAccountInfo.class,
                ReadyBalanceToAddressAdapter.class,
                TransferByAliasCase.class,
                SleepAdapter.$30SEC.class,
                SyncTxInfoCase.class,
                GetTranscationToAddressAdapter.class,
                SyncAccountBalance.class
        };
    }

    @Override
    public String title() {
        return "Alias transfer";
    }
}
