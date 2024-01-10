package io.nuls.test.cases.transcation.batch;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.account.facade.CreateAccountReq;
import io.nuls.base.api.provider.transaction.TransferService;
import io.nuls.base.data.NulsHash;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.cases.account.BaseAccountCase;
import io.nuls.test.cases.account.ImportAccountByPriKeyCase;
import io.nuls.test.cases.transcation.batch.fasttx.FastTransfer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-25 11:09
 * @Description: Function Description
 */
@Component
public class BatchCreateAccountCase2 extends BaseAccountCase<Long, BatchParam> {

    public static final long MAX_ACCOUNT = 5000L;

    public static final BigInteger TRANSFER_AMOUNT = BigInteger.valueOf(100000000L);

    public static final BigInteger FEE_AMOUNT = BigInteger.valueOf(10L);

    private List<String> formList = new ArrayList<>();

    private List<String> toList = new ArrayList<>();

    @Autowired
    ImportAccountByPriKeyCase importAccountByPriKeyCase;

    @Autowired
    FastTransfer fastTransfer;

    protected TransferService transferService = ServiceManager.get(TransferService.class);

    @Override
    public String title() {
        return "Batch preparation of accounts";
    }

    @Override
    public Long doTest(BatchParam param, int depth) throws TestFailException {
        formList.clear();
        toList.clear();
        String formAddress = importAccountByPriKeyCase.check(param.getFormAddressPriKey(), depth);
        int i = 0;
        int successTotal=0;
        Long start = System.currentTimeMillis();
        NulsHash perHash = null;
        Long total = param.count > MAX_ACCOUNT ? MAX_ACCOUNT : param.count;
        while (i < param.count) {
            i++;
            Result<String> account = accountService.createAccount(new CreateAccountReq(2, Constants.PASSWORD));
//            TransferReq.TransferReqBuilder builder =
//                    new TransferReq.TransferReqBuilder(config.getChainId(), config.getAssetsId())
//                            .addForm(formAddress, Constants.PASSWORD, TRANSFER_AMOUNT.multiply(FEE_AMOUNT));
//            builder.addTo(account.getList().get(0), TRANSFER_AMOUNT.multiply(FEE_AMOUNT));
//            builder.setRemark("remark");
//            Result<String> result = transferService.transfer(builder.build());
            Result<NulsHash> result = fastTransfer.transfer(formAddress,account.getList().get(0),TRANSFER_AMOUNT.multiply(FEE_AMOUNT),param.formAddressPriKey,perHash);
            try {
                checkResultStatus(result);
                perHash = result.getData();
                successTotal++;
            } catch (TestFailException e) {
                Log.error("Create transaction failed:{}",e.getMessage());
                continue;
            }
            result = fastTransfer.transfer(formAddress,account.getList().get(1),TRANSFER_AMOUNT.multiply(FEE_AMOUNT),param.formAddressPriKey,perHash);
            try {
                checkResultStatus(result);
                perHash = result.getData();
                successTotal++;
            } catch (TestFailException e) {
                Log.error("Create transaction failed:{}",e.getMessage());
                continue;
            }
            formList.add(account.getList().get(0));
            toList.add(account.getList().get(1));
        }
        Log.info("establish{}Transactions,success{}Pen, time-consuming:{}", MAX_ACCOUNT ,successTotal, System.currentTimeMillis() - start);
        return param.count;
    }

    public List<String> getFormList() {
        return formList;
    }

    public List<String> getToList() {
        return toList;
    }
}
