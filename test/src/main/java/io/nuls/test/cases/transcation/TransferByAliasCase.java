package io.nuls.test.cases.transcation;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.transaction.facade.TransferByAliasReq;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.cases.account.CreateAccountCase;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 15:00
 * @Description: 功能描述
 */
@Component
public class TransferByAliasCase extends BaseTranscationCase<String,String> {

    @Autowired
    CreateAccountCase createAccountCase;

    @Override
    public String title() {
        return null;
    }

    @Override
    public String doTest(String alias, int depth) throws TestFailException {
        String toAddress = createAccountCase.check(null,depth);
        Result<String> result = transferService.transferByAlias(new TransferByAliasReq(alias,toAddress, Constants.TRANSFER_AMOUNT,Constants.PASSWORD,Constants.REMARK));
        checkResultStatus(result);
        return result.getData();
    }
}
