package io.nuls.test.cases.account;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.GetAccountByAddressReq;
import io.nuls.base.api.provider.account.facade.ImportAccountByKeyStoreReq;
import io.nuls.test.cases.TestFailException;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;

import java.io.File;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 18:07
 * @Description: 功能描述
 */
@Component
public class ImportAccountKeystoreCase extends BaseAccountCase<String,String> {

    @Override
    public String title() {
        return "通过keystore导入账户";
    }

    @Override
    public String doTest(String param, int depth) throws TestFailException {
        String keystoreFile = System.getProperty("user.dir") + File.separator + param + ".keystore";
        String keystore = accountService.getAccountKeystoreDto(keystoreFile);
        Result<String> result = accountService.importAccountByKeyStore(new ImportAccountByKeyStoreReq(PASSWORD, HexUtil.encode(keystore.getBytes()),true));
        checkResultStatus(result);
        check(result.getData().equals(param),"导入地址与预期不一致");
        check(accountService.getAccountByAddress(new GetAccountByAddressReq(param)).getData() != null,"导入账户失败");
        new File(keystoreFile).delete();
        return param;
    }



}
