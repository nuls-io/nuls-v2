package io.nuls.test.cases.transcation.batch;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.account.AccountService;
import io.nuls.api.provider.account.facade.CreateAccountReq;
import io.nuls.api.provider.account.facade.GetAccountPrivateKeyByAddressReq;
import io.nuls.test.cases.CallRemoteTestCase;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.SleepAdapter;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.cases.transcation.TransferToAddressCase;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-25 12:08
 * @Description: 功能描述
 */
@Component
public class BatchReadyNodeAccountCase extends CallRemoteTestCase<Void,Integer> {

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Autowired
    TransferToAddressCase transferToAddressCase;

    @Autowired
    SleepAdapter.$15SEC sleep15;

    @Override
    public String title() {
        return "批量交易分解任务";
    }

    @Override
    public Void doTest(Integer total, int depth) throws TestFailException {
        List<String> nodes = getRemoteNodes();
        int itemCount = total / nodes.size();
        List<BatchParam> params = new ArrayList<>();
        Result<String> accounts = accountService.createAccount(new CreateAccountReq(nodes.size(), Constants.PASSWORD));
        for (int i = 0;i<accounts.getList().size();i++){
            String address = accounts.getList().get(i);
            String node = nodes.get(i);
            transferToAddressCase.check(address,depth);
            BatchParam bp = new BatchParam();
            bp.setCount(itemCount);
            bp.setFormAddressPriKey(accountService.getAccountPrivateKey(new GetAccountPrivateKeyByAddressReq(Constants.PASSWORD,address)).getData());
            Map<String,Object> res = doRemoteTest(node,BatchCreateAccountCase.class,bp);
            Log.info("res:{}",res);
        }
        sleep15.check(null,depth);
        for (int i = 0;i<accounts.getList().size();i++) {
            String node = nodes.get(i);
            Map res = doRemoteTest(node, BatchCreateTransferCase.class, itemCount);
            Log.info("res:{}", res);
        }
        return null;
    }
}
