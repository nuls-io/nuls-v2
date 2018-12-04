package io.nuls.ledger.test.service;

import io.nuls.ledger.model.AccountState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.test.BaseTest;
import io.nuls.tools.core.ioc.SpringLiteContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangkun23 on 2018/11/29.
 */
public class AccountStateServiceTest extends BaseTest {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void test() {
        Integer chainId = 1;
        String address = "NsdzTe4czMVA5Ccc1p9tgiGrKWx7WLNV";
        Integer assetId = 1;
        AccountStateService accountStateService = SpringLiteContext.getBean(AccountStateService.class);
        AccountState accountState = accountStateService.createAccount(address, chainId, assetId);

        logger.info("accountState {}", accountState);
        logger.info("test {}", accountStateService.isExist(address, chainId, assetId));
    }
}
