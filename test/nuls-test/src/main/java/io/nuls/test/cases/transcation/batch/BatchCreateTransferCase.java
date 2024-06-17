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

import static io.nuls.test.cases.Constants.REMARK;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-24 13:52
 * @Description: Function Description
 */
@Component
public class BatchCreateTransferCase extends BaseTranscationCase<Boolean, Long> {

    int THEADH_COUNT = 2;

    public static final BigInteger TRANSFER_AMOUNT = BigInteger.valueOf(10000000L);

    @Autowired
    BatchCreateAccountCase batchCreateAccountCase;

    @Override
    public String title() {
        return "Batch Create Transactions";
    }

    @Override
    public Boolean doTest(Long count, int depth) throws TestFailException {
        ThreadUtils.createAndRunThread("batch-start", () -> {
            AtomicInteger doneTotal = new AtomicInteger(0);
            AtomicInteger successTotal = new AtomicInteger(0);
            Long start = System.currentTimeMillis();
            Log.info("Start creating transaction");
            for (int s = 0; s < THEADH_COUNT; s++) {
                ThreadUtils.createAndRunThread("batch-transfer", () -> {
                    int i = doneTotal.getAndIncrement();
                    while (i < count) {
                        int index = i % batchCreateAccountCase.getFormList().size();
                        String formAddress = batchCreateAccountCase.getFormList().get(index);
                        String toAddress = batchCreateAccountCase.getToList().get(index);
                        TransferReq.TransferReqBuilder builder =
                                new TransferReq.TransferReqBuilder(config.getChainId(), config.getAssetsId())
                                        .addForm(formAddress, Constants.PASSWORD, TRANSFER_AMOUNT)
                                        .addTo(toAddress, TRANSFER_AMOUNT);
                        builder.setRemark(REMARK);
                        Result<String> result = transferService.transfer(builder.build(new TransferReq()));
                        try {
                            checkResultStatus(result);
                            successTotal.getAndIncrement();
                        } catch (TestFailException e) {
                            Log.error("Create transaction failed:{}", e.getMessage());
                        }
                        i = doneTotal.getAndIncrement();
                    }
                });
            }
            Log.info("establish{}Transactions,success{}Pen, time-consuming:{}", count, successTotal, System.currentTimeMillis() - start);
        });
        return true;
    }

}
