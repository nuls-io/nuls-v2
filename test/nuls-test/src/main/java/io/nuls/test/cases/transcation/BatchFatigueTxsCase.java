package io.nuls.test.cases.transcation;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.account.AccountService;
import io.nuls.base.api.provider.account.facade.CreateAccountReq;
import io.nuls.base.api.provider.account.facade.GetAccountPrivateKeyByAddressReq;
import io.nuls.base.api.provider.transaction.TransferService;
import io.nuls.base.api.provider.transaction.facade.TransferReq;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;
import io.nuls.test.Config;
import io.nuls.test.cases.CallRemoteTestCase;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.SleepAdapter;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.cases.transcation.batch.BatchCreateAccountCase;
import io.nuls.test.cases.transcation.batch.BatchCreateTransferCase;
import io.nuls.test.cases.transcation.batch.BatchParam;
import io.nuls.test.cases.transcation.batch.BatchTxsCase;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static io.nuls.test.cases.Constants.REMARK;
import static io.nuls.test.cases.transcation.batch.BatchCreateAccountCase.TRANSFER_AMOUNT;

/**
 * @Author: ljs
 * @Time: 2019-04-25 12:08
 * @Description: 功能描述
 */
@Component
public class BatchFatigueTxsCase extends CallRemoteTestCase<Void,Integer> {

    @Override
    public String title() {
        return "ljs test";
    }
    @Override
    public Void doTest(Integer total, int depth) throws TestFailException {
        List<String> nodes = getRemoteNodes();
        for (String n:nodes) {
            Boolean res = doRemoteTest(n, BatchTxsCase.class, n);
            Log.info("成功发起交易:{}", res);
        }
        return null;
    }
}
