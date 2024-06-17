package io.nuls.test.cases.account;

import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 10:30
 * @Description:
 * Account operation instruction integration test
 * The testing process is as follows
 * 0.Create an account.
 * 1.adopt1Address inquiry account in
 * 2.Back up this account to{user.dir}catalogue
 * 3.Delete this account
 * 4.adopt【2】MiddlekeystoreImport account
 * 5.adoptaddressCheck the private key of this account with the password
 * 6.Delete the account and import it again using the private key
 * 7.Change account password
 * 8.Query the account list and verify if there are any secondary accounts in the list
 * 9.Delete this account, delete【2】Middlekeystore
 */
@TestCase("account")
@Component
public class AccountCase extends TestCaseChain {

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                CreateAccountCase.class,
                GetAccountByAddressCase.class,
                GetAddressByAccountInfoAdapder.class,
                BackupAccountCase.class,
                RemoveAccountCase.class,
                ImportAccountKeystoreCase.class,
                GetAccountPriKeyCase.class,
                ImportAccountByPriKeyCase.class,
                UpdatePasswordCase.class,
                GetAccountListCase.class
        };
    }

    @Override
    public String title() {
        return "Account module integration testing";
    }

}
