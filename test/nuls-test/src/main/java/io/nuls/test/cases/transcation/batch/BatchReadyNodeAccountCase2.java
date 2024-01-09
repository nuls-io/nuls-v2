package io.nuls.test.cases.transcation.batch;

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
import io.nuls.test.cases.*;
import io.nuls.test.cases.transcation.TransferToAddressCase;
import org.checkerframework.checker.units.qual.C;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.nuls.test.cases.Constants.REMARK;
import static io.nuls.test.cases.transcation.batch.BatchCreateAccountCase2.*;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-25 12:08
 * @Description: Function Description
 */
@Component
@TestCase("batchTransfer2")
public class BatchReadyNodeAccountCase2 extends CallRemoteTestCase<Void, Long> {

    AccountService accountService = ServiceManager.get(AccountService.class);

    TransferService transferService = ServiceManager.get(TransferService.class);

    private ThreadPoolExecutor threadPoolExecutor = ThreadUtils.createThreadPool(5, 5, new NulsThreadFactory("create-account"));

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
        return "Local debugging batch creation of transactions";
    }

    @Override
    public Long initParam() {
        return config.getBatchTxTotal();
    }

    @Override
    public Void doTest(Long total, int depth) throws TestFailException {
        List<String> nodes = getRemoteNodes();
        Long itemCount = total / nodes.size();
        List<BatchParam> params = new ArrayList<>();
        //Create a transfer account for each node to transfer assets to several withdrawal addresses
        Result<String> accounts = accountService.createAccount(new CreateAccountReq(nodes.size(), Constants.PASSWORD));
        //The total number of assets transferred to the intermediary account is equal to The total number of accounts participating in a single node * 10000NULSHandling fees for（Enough10010000 times） +
        BigInteger amount =
                TRANSFER_AMOUNT
                        //Calculate the maximum number of accounts
                        .multiply(BigInteger.valueOf(itemCount > MAX_ACCOUNT ? MAX_ACCOUNT : itemCount)).multiply(BigInteger.TWO)
                        //Assign to each account1000Handling fees for consumption
                        .multiply(FEE_AMOUNT)
                        //The handling fee used for the allocation of this account
                        .add(BigInteger.valueOf(itemCount / 1000).multiply(BigInteger.TWO).multiply(TRANSFER_AMOUNT));
        Log.info("The total number of accounts for each node:{}", (itemCount > MAX_ACCOUNT ? MAX_ACCOUNT : itemCount) * 2);
        Log.info("Total number of assets prepared for each intermediate account:{}", amount);
        for (int i = 0; i < accounts.getList().size(); i++) {
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
            bp.setCount(itemCount);
            bp.setFormAddressPriKey(accountService.getAccountPrivateKey(new GetAccountPrivateKeyByAddressReq(Constants.PASSWORD, address)).getData());
            params.add(bp);
        }
        sleep30.check(null, depth);
        for (int i = 0; i < nodes.size(); i++) {
            String node = nodes.get(i);
            BatchParam bp = params.get(i);
            Integer res = doRemoteTest(node, BatchCreateAccountCase2.class, bp);
            Log.info("Successfully created test account{}individual", res);
        }
        sleep60.check(null, depth);
        BatchParam bp = new BatchParam();
        bp.setCount(itemCount);
        bp.setReverse(false);
        while(true){
            CountDownLatch latch = new CountDownLatch(nodes.size());
            for (int i = 0; i < accounts.getList().size(); i++) {
                String node = nodes.get(i);
                ThreadUtils.createAndRunThread("batch-transfer-" + i , () -> {
                    Boolean res = null;
                    try {
                        res = doRemoteTest(node, BatchCreateTransferCase2.class, bp);
                        Log.info("Successfully initiated transaction:{}", res);
                        latch.countDown();
                    } catch (TestFailException e) {
                        Log.error(e.getMessage(),e);
                        latch.countDown();
                    }
                });
            }
            try {
                latch.await();
                TimeUnit.SECONDS.sleep(15);
                bp.setReverse(!bp.getReverse());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.info("Create transaction completed");
        }
//        return null;
    }
}
