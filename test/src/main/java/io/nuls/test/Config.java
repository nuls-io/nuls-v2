package io.nuls.test;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.account.AccountService;
import io.nuls.api.provider.account.facade.ImportAccountByPrivateKeyReq;
import io.nuls.test.cases.account.AccountConstants;
import io.nuls.test.cases.account.BaseAccountCase;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Value;
import io.nuls.tools.exception.NulsException;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 14:31
 * @Description: 功能描述
 */
@Configuration(persistDomain = "test")
@Data
public class Config implements InitializingBean {

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Value("testNodeExclude")
    String nodeExclude;

    @Value("testSeedAccountPriKey")
    String testSeedAccount;

    int chainId;

    int assetsId;

    String seedAddress;

    @Override
    public void afterPropertiesSet() throws NulsException {
        Result<String> result = accountService.importAccountByPrivateKey(new ImportAccountByPrivateKeyReq(AccountConstants.PASSWORD,testSeedAccount,true));
        this.seedAddress = result.getData();
    }
}
