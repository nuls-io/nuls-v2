package io.nuls.cmd.client.processor.account;

import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.account.AccountService;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.cmd.client.processor.CommandGroup;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 17:04
 * @Description:
 */
public abstract class AccountBaseProcessor implements CommandProcessor {

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.Account;
    }

}
