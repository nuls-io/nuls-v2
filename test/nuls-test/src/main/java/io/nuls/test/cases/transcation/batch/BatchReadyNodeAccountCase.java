package io.nuls.test.cases.transcation.batch;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.account.AccountService;
import io.nuls.base.api.provider.account.facade.CreateAccountReq;
import io.nuls.base.api.provider.account.facade.GetAccountPrivateKeyByAddressReq;
import io.nuls.base.api.provider.transaction.TransferService;
import io.nuls.base.api.provider.transaction.facade.TransferReq;
import io.nuls.test.Config;
import io.nuls.test.cases.*;
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
import static io.nuls.test.cases.transcation.batch.BatchCreateAccountCase.MAX_ACCOUNT;
import static io.nuls.test.cases.transcation.batch.BatchCreateAccountCase.TRANSFER_AMOUNT;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-25 12:08
 * @Description: 功能描述
 */
@Component
@TestCase("batchTransfer")
public class BatchReadyNodeAccountCase extends CallRemoteTestCase<Void, Long> {

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
        return "本地调试批量创建交易";
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
        //给每个节点创建一个中转账户，用于把资产转账到若干出金地址中
        Result<String> accounts = accountService.createAccount(new CreateAccountReq(nodes.size(), Constants.PASSWORD));
        //转给中间账户的资产总数等于 单个节点参与账户总数 * 10000NULS的手续费（够用100万次） +
        BigInteger amount =
                TRANSFER_AMOUNT
                        //计算最大账户数
                        .multiply(BigInteger.valueOf(itemCount > MAX_ACCOUNT ? MAX_ACCOUNT : itemCount)).multiply(BigInteger.TWO)
                        //每个账户分配1000个作为消耗的手续费
                        .multiply(BigInteger.valueOf(100L))
                        //本账户分配时使用的手续费
                        .add(BigInteger.valueOf(itemCount / 1000).multiply(BigInteger.TWO).multiply(TRANSFER_AMOUNT));
        Log.info("每个节点的账户总数:{}", (itemCount > MAX_ACCOUNT ? MAX_ACCOUNT : itemCount) * 2);
        Log.info("每个中间账户准备资产总数:{}", amount);
        for (int i = 0; i < accounts.getList().size(); i++) {
            String address = accounts.getList().get(i);
            String formAddress = config.getSeedAddress();
            TransferReq.TransferReqBuilder builder =
                    new TransferReq.TransferReqBuilder(config.getChainId(), config.getAssetsId())
                            .addForm(formAddress, Constants.PASSWORD, amount)
                            .addTo(address, amount);
            builder.setRemark(REMARK);
            Result<String> result = transferService.transfer(builder.build());
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
            Integer res = doRemoteTest(node, BatchCreateAccountCase.class, bp);
            Log.info("成功创建测试账户{}个", res);
        }
        sleep60.check(null, depth);
//        for (int i = 0;i<accounts.getList().size();i++) {
//            String node = nodes.get(i);
//            Boolean res = doRemoteTest(node, BatchCreateTransferCase.class, itemCount);
//            Log.info("成功发起交易:{}", res);
//        }
        return null;
    }
}
