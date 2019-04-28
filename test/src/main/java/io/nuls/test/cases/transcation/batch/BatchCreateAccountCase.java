package io.nuls.test.cases.transcation.batch;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.account.facade.CreateAccountReq;
import io.nuls.api.provider.transaction.TransferService;
import io.nuls.api.provider.transaction.facade.TransferReq;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.cases.account.BaseAccountCase;
import io.nuls.test.cases.account.ImportAccountByPriKeyCase;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;
import org.spongycastle.asn1.cms.OtherKeyAttribute;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import static io.nuls.test.cases.Constants.REMARK;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-25 11:09
 * @Description: 功能描述
 */
@Component
public class BatchCreateAccountCase extends BaseAccountCase<Integer, BatchParam> {

    public static final BigInteger TRANSFER_AMOUNT = BigInteger.valueOf(100000000L);

    private List<String> formList = new ArrayList<>();

    private List<String> toList = new ArrayList<>();

    @Autowired
    ImportAccountByPriKeyCase importAccountByPriKeyCase;

    protected TransferService transferService = ServiceManager.get(TransferService.class);

    @Override
    public String title() {
        return "批量准备账户";
    }

    @Override
    public Integer doTest(BatchParam param, int depth) throws TestFailException {
        formList.clear();
        toList.clear();
        String formAddress = importAccountByPriKeyCase.check(param.getFormAddressPriKey(), depth);
        int i = 0;
        int successTotal=0;
        Long start = System.currentTimeMillis();
        while (i < param.count) {
            i++;
            Result<String> account = accountService.createAccount(new CreateAccountReq(2, Constants.PASSWORD));
            TransferReq.TransferReqBuilder builder =
                    new TransferReq.TransferReqBuilder(config.getChainId(), config.getAssetsId())
                            .addForm(formAddress, Constants.PASSWORD, TRANSFER_AMOUNT);
            builder.addTo(account.getList().get(0), TRANSFER_AMOUNT);
            builder.setRemark(REMARK);
            Result<String> result = transferService.transfer(builder.build());
            try {
                checkResultStatus(result);
                successTotal++;
            } catch (TestFailException e) {
                Log.error("创建交易失败:{}",e.getMessage());
                continue;
            }
            formList.add(account.getList().get(0));
            toList.add(account.getList().get(1));
        }
        Log.info("创建{}笔交易,成功{}笔，消耗时间:{}", param.count,successTotal, System.currentTimeMillis() - start);
        return toList.size();
    }

    public List<String> getFormList() {
        return formList;
    }

    public List<String> getToList() {
        return toList;
    }
}
