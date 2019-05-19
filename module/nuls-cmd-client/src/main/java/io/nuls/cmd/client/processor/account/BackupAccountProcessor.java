package io.nuls.cmd.client.processor.account;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.BackupAccountReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 16:54
 * @Description: 导出账户到keystore文件中
 */
@Component
public class BackupAccountProcessor extends AccountBaseProcessor implements CommandProcessor {


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
        checkArgsNumber(args,1,2);
        checkAddress(config.getChainId(),args[1]);
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        String path = args.length == 3 ? args[2] : "";
        String password = getPwd();
        BackupAccountReq req = new BackupAccountReq(password, address, path);
        Result<String> res = accountService.backupAccount(req);
        if (!res.isSuccess()) {
            return CommandResult.getFailed(res);
        }
        return CommandResult.getSuccess("The path to the backup file is " + res.getData());
    }
}