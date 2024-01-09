package io.nuls.test.cases.transcation;

import io.nuls.test.Config;
import io.nuls.test.cases.SleepAdapter;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.test.cases.account.CreateAccountCase;
import io.nuls.test.cases.ledger.SyncAccountBalance;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 21:12
 * @Description:
 * Transaction related instruction testing
 * Regular transaction testing
 * 1.Create an account
 * 2.From seed address{@link Config#getSeedAddress()}Transfer10individualNulsreach【1】Address in
 * 3.wait for10Second, check if the transaction is confirmed
 * 4.Check if the account balance has increased10individualNULS(Need to deduct handling fees）
 * 5.Check if the balance of the seed address has decreased10individualNULS
 * 6.Remote node queries whether this transaction is confirmed
 * 7.Remote import this account and check if the balance matches the local account
 */
@Component
public class TransferCase extends TestCaseChain {

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                CreateAccountCase.class,
                TransferToAddressCase.class,
                SleepAdapter.$30SEC.class,
                SyncTxInfoCase.class,
                GetTranscationToAddressAdapter.class,
                SyncAccountBalance.class
        };
    }

    @Override
    public String title() {
        return "Ordinary transfer transaction";
    }
}
