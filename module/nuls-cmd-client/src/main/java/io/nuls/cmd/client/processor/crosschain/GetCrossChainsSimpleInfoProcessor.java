package io.nuls.cmd.client.processor.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.CrossChainRegisterInfo;
import io.nuls.base.api.provider.crosschain.facade.GetCrossChainInfoReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.core.core.annotation.Component;

import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-07 13:41
 * @Description: 功能描述
 */
@Component
public class GetCrossChainsSimpleInfoProcessor extends CrossChainBaseProcessor {

    @Override
    public String getCommand() {
        return "crosschains";
    }

    @Override
    public String getHelp() {
        return new CommandBuilder()
                .newLine(getCommandDescription())
                .toString();
    }

    @Override
    public String getCommandDescription() {
        return getCommand() + " no parameter";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,0);
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        Result<Map> result = chainManageProvider.getCrossChainsSimpleInfo();
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result);
    }
}
