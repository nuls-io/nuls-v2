package io.nuls.cmd.client.processor.account;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.account.AccountService;
import io.nuls.api.provider.account.facade.BackupAccountReq;
import io.nuls.base.basic.AddressTool;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.Config;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 16:54
 * @Description: 导出账户到keystore文件中
 */
@Component
public class BackupAccountProcessor implements CommandProcessor {

    AccountService accountService = ServiceManager.get(AccountService.class);

    @Autowired
    Config config;

    @Override
    public String getCommand() {
        return "backup";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> the account you want to back up - Required")
                .newLine("\t[path] The folder of the export file, defaults to the current directory");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "backup <address> [path] --backup the account key store";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length < 2 || length > 3) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (!AddressTool.validAddress(config.getChainId(), args[1])) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        String path = args.length == 3 ? args[2] : "";
        String password = CommandHelper.getPwd(null);
        BackupAccountReq req = new BackupAccountReq(password, address, path);
        Result<String> res = accountService.backupAccount(req);
        if (!res.isSuccess()) {
            return CommandResult.getFailed(res);
        }
        return CommandResult.getSuccess("The path to the backup file is " + res.getData());
    }
}