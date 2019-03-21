package io.nuls.test.cases.transcation;

import io.nuls.test.cases.Sleep10SecCase;
import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.test.cases.account.SyncAccountInfo;
import io.nuls.test.cases.ledger.SyncAccountBalance;
import io.nuls.tools.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 14:36
 * @Description:
 * 设置别名测试
 * 1.设置别名
 * 2.等待10秒
 * 3.检查设置别名交易是否已确认
 * 4.检查本地别名是否已设置成功
 * 5.检查网络节点账户信息是否一致
 */
@TestCase("setAlias")
@Component
public class AliasTransferCase extends TestCaseChain {

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                SetAliasCase.class,
                Sleep10SecCase.class,
                GetTxInfoCase.class,
                GetTranscationFormAddressCase.class,
                CheckAliasCase.class,
                SyncAccountInfo.class,
                TransferByAliasCase.class,
                GetTxInfoCase.class,
                GetTranscationToAddressCase.class,
                SyncAccountBalance.class
        };
    }

    @Override
    public String title() {
        return null;
    }
}
