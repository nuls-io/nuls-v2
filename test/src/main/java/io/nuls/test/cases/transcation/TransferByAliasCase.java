package io.nuls.test.cases.transcation;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.account.AccountService;
import io.nuls.api.provider.account.facade.AccountInfo;
import io.nuls.api.provider.account.facade.GetAccountByAddressReq;
import io.nuls.api.provider.transaction.facade.TransferByAliasReq;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.TestFailException;
import io.nuls.test.cases.account.CreateAccountCase;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.model.StringUtils;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 15:00
 * @Description: 功能描述
 */
@Component
public class TransferByAliasCase extends BaseTranscationCase<String,String> {

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Autowired
    CreateAccountCase createAccountCase;

    @Override
    public String title() {
        return "通过别名转账到指定地址";
    }

    @Override
    public String doTest(String address, int depth) throws TestFailException {
        String toAddress = createAccountCase.check(null,depth);
        AccountInfo accountInfo = accountService.getAccountByAddress(new GetAccountByAddressReq(address)).getData();
        check(StringUtils.isNotBlank(accountInfo.getAlias()),"别名转账异常，转出账户别名为空");
        Result<String> result = transferService.transferByAlias(new TransferByAliasReq(accountInfo.getAlias(),toAddress, Constants.TRANSFER_AMOUNT,Constants.PASSWORD,Constants.REMARK));
        checkResultStatus(result);
        return result.getData();
    }
}
