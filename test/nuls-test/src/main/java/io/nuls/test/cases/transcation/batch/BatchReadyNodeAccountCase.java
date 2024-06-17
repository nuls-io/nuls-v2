package io.nuls.test.cases.transcation.batch;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.account.AccountService;
import io.nuls.base.api.provider.account.facade.CreateAccountReq;
import io.nuls.base.api.provider.account.facade.GetAccountPrivateKeyByAddressReq;
import io.nuls.base.api.provider.transaction.TransferService;
import io.nuls.base.api.provider.transaction.facade.TransferReq;
import io.nuls.test.Config;
import io.nuls.test.cases.CallRemoteTestCase;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.SleepAdapter;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.cases.transcation.TransferToAddressCase;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static io.nuls.test.cases.Constants.REMARK;
import static io.nuls.test.cases.transcation.batch.BatchCreateAccountCase.TRANSFER_AMOUNT;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-25 12:08
 * @Description: Function Description
 */
@Component
public class BatchReadyNodeAccountCase extends CallRemoteTestCase<Void,Integer> {

    AccountService accountService = ServiceManager.get(AccountService.class);

    TransferService transferService = ServiceManager.get(TransferService.class);

    private ThreadPoolExecutor threadPoolExecutor = ThreadUtils.createThreadPool(5,5,new NulsThreadFactory("create-account"));

    @Autowired
    Config config;

    @Autowired
    TransferToAddressCase transferToAddressCase;

    @Autowired
    SleepAdapter.$30SEC sleep30;
    @Autowired
    SleepAdapter.$15SEC sleep15;

    @Autowired
    SleepAdapter.$60SEC sleep60;

    @Override
    public String title() {
        return "Batch transaction decomposition task";
    }

    @Override
    public Void doTest(Integer total, int depth) throws TestFailException {
        List<String> nodes = getRemoteNodes();
        int itemCount = total / nodes.size();
        List<BatchParam> params = new ArrayList<>();
        //Create a transfer account for each node to transfer assets to several withdrawal addresses
        Result<String> accounts = accountService.createAccount(new CreateAccountReq(nodes.size(), Constants.PASSWORD));
        BigInteger amount = TRANSFER_AMOUNT.multiply(BigInteger.valueOf(itemCount)).multiply(BigInteger.TWO);
        for (int i = 0;i<accounts.getList().size();i++){
            String address = accounts.getList().get(i);
            String formAddress = config.getSeedAddress();
            TransferReq.TransferReqBuilder builder =
                    new TransferReq.TransferReqBuilder(config.getChainId(), config.getAssetsId())
                            .addForm(formAddress, Constants.PASSWORD, amount)
                            .addTo(address, amount);
            builder.setRemark(REMARK);
            Result<String> result = transferService.transfer(builder.build(new TransferReq()));
            checkResultStatus(result);
            BatchParam bp = new BatchParam();
            bp.setCount((long)itemCount);
            bp.setFormAddressPriKey(accountService.getAccountPrivateKey(new GetAccountPrivateKeyByAddressReq(Constants.PASSWORD,address)).getData());
            params.add(bp);
        }
        sleep30.check(null,depth);
//        CountDownLatch latch = new CountDownLatch(nodes.size());
        for (int i = 0;i<nodes.size();i++){
            String node = nodes.get(i);
            BatchParam bp = params.get(i);
            Integer res = doRemoteTest(node, BatchCreateAccountCase.class,bp);
//            threadPoolExecutor.execute(()->{
//                Integer res = null;
//                try {
//                } catch (TestFailException e) {
//                    e.printStackTrace();
//                }
//                latch.countDown();
//            });
            Log.info("Successfully created test account{}individual",res);
        }
//        try {
//            latch.await(60L, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        sleep60.check(null,depth);
        for (int i = 0;i<accounts.getList().size();i++) {
            String node = nodes.get(i);
            Boolean res = doRemoteTest(node, BatchCreateTransferCase.class, itemCount);
            Log.info("Successfully initiated transaction:{}", res);
        }
        return null;
    }
}
