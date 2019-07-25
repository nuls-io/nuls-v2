package io.nuls.cmd.client.processor.transaction;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.transaction.facade.CreateMultiSignTransferRes;
import io.nuls.base.api.provider.transaction.facade.SignMultiSignTransferReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-07-24 14:46
 * @Description: 对多签交易进行签名
 */
@Component
public class SignMultiSingTransferProcessor  extends TransactionBaseProcessor implements CommandProcessor {

    @Autowired
    Config config;


    @Override
    public String getCommand() {
        return "signmultisigntransfer";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<sign address> \t\tsource address or alias - Required")
                .newLine("\t<tx content> \treceiving address or alias - Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "signmultisigntransfer <sign address> <tx> ";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,2);
        return true;
    }


    @Override
    public CommandResult execute(String[] args) {
        String signAddress = args[1];
        String tx = args[2];
        String pwd = getPwd();
        Result<CreateMultiSignTransferRes> result = transferService.signMultiSignTransfer(new SignMultiSignTransferReq(tx,signAddress,pwd));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result);
    }

}