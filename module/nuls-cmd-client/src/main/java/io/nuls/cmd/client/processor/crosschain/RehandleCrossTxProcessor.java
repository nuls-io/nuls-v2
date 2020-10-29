package io.nuls.cmd.client.processor.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.block.BlockService;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.nuls.base.api.provider.crosschain.facade.GetCrossTxStateReq;
import io.nuls.base.api.provider.crosschain.facade.RehandleCtxReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-07 13:47
 * @Description: 功能描述
 */
@Component
public class RehandleCrossTxProcessor extends CrossChainBaseProcessor {

    BlockService blockService = ServiceManager.get(BlockService.class);

    @Override
    public String getCommand() {
        return "rehandlectx";
    }

    @Override
    public String getHelp() {
        return new CommandBuilder()
                .newLine(getCommandDescription())
                .newLine("\t<txHash>  tx hash - require")
                .toString();
    }

    @Override
    public String getCommandDescription() {
        return getCommand() + " <txHash> ";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1);
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String txHash = args[1];
        long blockHeight = blockService.getBlockHeaderByLastHeight(new GetBlockHeaderByLastHeightReq()).getData().getHeight();
        Result<String> result = crossChainProvider.rehandleCtx(new RehandleCtxReq(txHash,blockHeight));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result.getData());
    }
}
