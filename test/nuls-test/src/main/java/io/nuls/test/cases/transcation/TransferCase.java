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
 * 交易相关指令测试
 * 普通交易测试
 * 1.创建一个账户
 * 2.从种子地址{@link Config#getSeedAddress()}转账10个Nuls到【1】中的地址
 * 3.等待10秒，查询交易是否确认
 * 4.查询账户余额是否增加10个NULS(需要减去手续费）
 * 5.查询种子地址余额是否减少10个NULS
 * 6.远程节点查询此交易是否确认
 * 7.远程导入此账户，查询余额是否与本地一致
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
        return "普通转账交易";
    }
}
