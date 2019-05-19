package io.nuls.cmd.client.processor.account;

import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.account.AccountService;
import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.cmd.client.processor.CommandGroup;
import io.nuls.core.core.annotation.Autowired;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 17:04
 * @Description:
 */
public abstract class AccountBaseProcessor implements CommandProcessor {

    @Autowired
    Config config;

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.Account;
    }

}
