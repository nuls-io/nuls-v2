package io.nuls.test.cases.transcation;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.account.AccountService;
import io.nuls.api.provider.account.facade.CreateAccountReq;
import io.nuls.api.provider.transaction.facade.TransferReq;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.cases.account.ImportAccountByPriKeyCase;
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
@TestCase("batchTransfer")
public class BatchCreateTransferCase extends BaseTranscationCase<Boolean, BatchCreateTransferCase.BatchCreateTransferParam> {

    public static final BigInteger TRANSFER_AMOUNT = BigInteger.valueOf(10000000L);

    public static class BatchCreateTransferParam {
        int count;
        String formAddressPriKey;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getFormAddressPriKey() {
            return formAddressPriKey;
        }

        public void setFormAddressPriKey(String formAddressPriKey) {
            this.formAddressPriKey = formAddressPriKey;
        }
    }

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Autowired
    ImportAccountByPriKeyCase importAccountByPriKeyCase;

    @Override
    public String title() {
        return "批量创建交易";
    }

    @Override
    public Boolean doTest(BatchCreateTransferParam param, int depth) throws TestFailException {
        String formAddress = importAccountByPriKeyCase.check(param.getFormAddressPriKey(),depth);
        ThreadUtils.createAndRunThread("batch-transfer",()->{
            int i = 0;
            while(true){
                if(i>param.count){
                    break;
                }
                Result<String> account = accountService.createAccount(new CreateAccountReq(1, Constants.PASSWORD));
                TransferReq.TransferReqBuilder builder =
                        new TransferReq.TransferReqBuilder(config.getChainId(), config.getAssetsId())
                                .addForm(formAddress, Constants.PASSWORD, TRANSFER_AMOUNT)
                                .addTo(account.getList().get(0), TRANSFER_AMOUNT);
                builder.setRemark(REMARK);
                Result<String> result = transferService.transfer(builder.build());
                try {
                    checkResultStatus(result);
                } catch (TestFailException e) {
                    Log.error("创建交易失败");
                }
            }
        });
        return true;
    }

    @Override
    public BatchCreateTransferParam initParam() {
        BatchCreateTransferParam param = new BatchCreateTransferParam();
        param.setCount(1);
        param.setFormAddressPriKey(config.getTestSeedAccount());
        return param;
    }
}
