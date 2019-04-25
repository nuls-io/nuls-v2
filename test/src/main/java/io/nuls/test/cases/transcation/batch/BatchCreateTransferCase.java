package io.nuls.test.cases.transcation.batch;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.account.AccountService;
import io.nuls.api.provider.transaction.facade.TransferReq;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.cases.transcation.BaseTranscationCase;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.ThreadUtils;

import java.math.BigInteger;

import static io.nuls.test.cases.Constants.REMARK;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-24 13:52
 * @Description: 功能描述
 */
@Component
public class BatchCreateTransferCase extends BaseTranscationCase<Boolean, Integer> {

    public static final BigInteger TRANSFER_AMOUNT = BigInteger.valueOf(10000000L);

    @Autowired BatchCreateAccountCase batchCreateAccountCase;

    @Override
    public String title() {
        return "批量创建交易";
    }

    @Override
    public Boolean doTest(Integer count, int depth) throws TestFailException {
        ThreadUtils.createAndRunThread("batch-transfer", () -> {
            int i = 0;
            int successTotal = 0;
            Long start = System.currentTimeMillis();
            while (i < count) {
                int index = i % batchCreateAccountCase.getFormList().size();
                String formAddress = batchCreateAccountCase.getFormList().get(index);
                String toAddress = batchCreateAccountCase.getToList().get(index);
                i++;
                TransferReq.TransferReqBuilder builder =
                        new TransferReq.TransferReqBuilder(config.getChainId(), config.getAssetsId())
                                .addForm(formAddress, Constants.PASSWORD, TRANSFER_AMOUNT)
                                .addTo(toAddress, TRANSFER_AMOUNT);
                builder.setRemark(REMARK);
                Result<String> result = transferService.transfer(builder.build());
                try {
                    checkResultStatus(result);
                    successTotal++;
                } catch (TestFailException e) {
                    Log.error("创建交易失败:{}",e.getMessage());
                }
            }
            Log.info("创建{}笔交易,成功{}笔，消耗时间:{}", count,successTotal, System.currentTimeMillis() - start);
        });
        return true;
    }

}
