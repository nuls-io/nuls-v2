package io.nuls.test.cases.transcation;

import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.account.AccountService;
import io.nuls.base.api.provider.account.facade.AccountInfo;
import io.nuls.base.api.provider.account.facade.GetAccountByAddressReq;
import io.nuls.test.cases.Constants;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 14:56
 * @Description: Function Description
 *
 */
@Component
public class CheckAliasCase extends BaseTranscationCase<String,String> {

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Override
    public String title() {
        return "Is the alias set successfully";
    }

    @Override
    public String doTest(String address, int depth) throws TestFailException {
        AccountInfo accountInfo = accountService.getAccountByAddress(new GetAccountByAddressReq(address)).getData();
        if(!Constants.getAlias(address).equals(accountInfo.getAlias())){
            throw new TestFailException("Account alias does not meet expectations");
        }
        return address;
    }
}
