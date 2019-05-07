package io.nuls.cmd.client.processor.crosschain;

import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-06 17:34
 * @Description: 功能描述
 */
public class CreateCrossTxProcessor extends CrossChainBaseProcessor {

    @Override
    public String getCommand() {
        return "createcrosstx";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> the account address - require");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createcrosstx <address> --get the balance of a address";
    }


    @Override
    public boolean argsValidate(String[] args) {
        return false;
    }

    @Override
    public CommandResult execute(String[] args) {
        return null;
    }
}
