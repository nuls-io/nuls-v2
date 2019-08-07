package io.nuls.cmd.client.processor.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.CrossAssetRegisterInfo;
import io.nuls.base.api.provider.crosschain.facade.CrossChainRegisterInfo;
import io.nuls.base.api.provider.crosschain.facade.GetCrossAssetInfoReq;
import io.nuls.base.api.provider.crosschain.facade.GetCrossChainInfoReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-07 13:41
 * @Description: 功能描述
 */
@Component
public class GetCrossAssetInfoProcessor extends CrossChainBaseProcessor {

    @Override
    public String getCommand() {
        return "crossassetinfo";
    }

    @Override
    public String getHelp() {
        return new CommandBuilder()
                .newLine(getCommandDescription())
                .newLine("\t<chainId>  register chain id - require")
                .newLine("\t<assetId>  register asset id - require")
                .toString();
    }

    @Override
    public String getCommandDescription() {
        return getCommand() + " <chainId> chain id - require";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,2);
        checkIsNumeric(args[1],"chainId");
        checkIsNumeric(args[2],"assetId");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        Integer chainId = Integer.parseInt(args[1]);
        Integer assetId = Integer.parseInt(args[2]);
        Result<CrossAssetRegisterInfo> result = chainManageProvider.getCrossAssetInfo(new GetCrossAssetInfoReq(chainId,assetId));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result);
    }
}
