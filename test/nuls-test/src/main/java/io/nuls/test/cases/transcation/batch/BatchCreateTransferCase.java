package io.nuls.test.cases.transcation.batch;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.transaction.facade.TransferReq;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.cases.transcation.BaseTranscationCase;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.thread.ThreadUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static io.nuls.test.cases.Constants.REMARK;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-24 13:52
 * @Description: 功能描述
 */
@Component
public class BatchCreateTransferCase extends BaseTranscationCase<Boolean, Long> {

    int THEADH_COUNT = 2;

    public static final BigInteger TRANSFER_AMOUNT = BigInteger.valueOf(10000000L);

    @Autowired
    BatchCreateAccountCase batchCreateAccountCase;

    @Override
    public String title() {
        return "批量创建交易";
    }

    @Override
    public Boolean doTest(Long count, int depth) throws TestFailException {
        ThreadUtils.createAndRunThread("batch-start", () -> {
            AtomicLong doneTotal = new AtomicLong(0);
            AtomicLong successTotal = new AtomicLong(0);
            CountDownLatch latch = new CountDownLatch(THEADH_COUNT);
            Long start = System.currentTimeMillis();
            Log.info("开始创建交易");
            //每个线程需要执行的交易总数
            long threadExecTotal = count / THEADH_COUNT;
            //每个线程分配到的账户总数
            int threadAccountTotal = batchCreateAccountCase.getFormList().size() / THEADH_COUNT;
            for (int s = 0; s < THEADH_COUNT; s++) {
                //当前线程在账户列表中获取账户的索引偏移值
                int offset = s * (batchCreateAccountCase.getFormList().size() / THEADH_COUNT);
                ThreadUtils.createAndRunThread("batch-transfer", () -> {
                    long i = doneTotal.getAndIncrement();
                    List<String> form = batchCreateAccountCase.getFormList();
                    List<String> to = batchCreateAccountCase.getToList();
                    boolean flag = true;
                    while (i < threadExecTotal) {
                        int index = (int) (i % threadAccountTotal);
                        String formAddress = form.get(offset + index);
                        String toAddress = to.get(offset + index);
                        TransferReq.TransferReqBuilder builder =
                                new TransferReq.TransferReqBuilder(config.getChainId(), config.getAssetsId())
                                        .addForm(formAddress, Constants.PASSWORD, TRANSFER_AMOUNT)
                                        .addTo(toAddress, TRANSFER_AMOUNT);
                        builder.setRemark(REMARK);
                        Result<String> result = transferService.transfer(builder.build());
                        try {
                            checkResultStatus(result);
                            successTotal.getAndIncrement();
                        } catch (TestFailException e) {
                            Log.error("创建交易失败:{}", e.getMessage());
                        }
                        i = doneTotal.getAndIncrement();
                        if(index == threadAccountTotal - 1){
                            if(flag){
                                form = batchCreateAccountCase.getToList();
                                to = batchCreateAccountCase.getFormList();
                                flag = false;
                            }else{
                                form = batchCreateAccountCase.getFormList();
                                to = batchCreateAccountCase.getToList();
                                flag = true;
                            }
                        }
                    }
                    latch.countDown();
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.info("创建{}笔交易,成功{}笔，消耗时间:{}", count, successTotal, System.currentTimeMillis() - start);
        });
        return true;
    }

}
