package io.nuls.cmd.client.processor.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.GetCrossTxStateReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-07 13:47
 * @Description: 功能描述
 */
@Component
public class GetCrossTxStateProcessor extends CrossChainBaseProcessor {

    @Override
    public String getCommand() {
        return "getcrosstxstate";
    }

    @Override
    public String getHelp() {
        return new CommandBuilder()
                .newLine(getCommandDescription())
                .newLine("\t<chainId>  target chain id - require")
                .newLine("\t<txHash>  tx hash - require")
                .toString();
    }

    @Override
    public String getCommandDescription() {
        return getCommand() + " <chainId> <txHash> ";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,2);
        checkIsNumeric(args[1],"chainId");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        Integer chainId = Integer.parseInt(args[1]);
        String txHash = args[2];
        Result<Boolean> result = crossChainProvider.getCrossTxState(new GetCrossTxStateReq(chainId,txHash));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result.getData() ? "Confirmed" : "Unconfirmed");
    }
}
