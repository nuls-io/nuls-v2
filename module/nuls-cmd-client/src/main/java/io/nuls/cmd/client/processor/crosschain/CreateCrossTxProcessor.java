package io.nuls.cmd.client.processor.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.CreateCrossTxReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.core.core.annotation.Component;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-06 17:34
 * @Description: 功能描述
 */
@Component
public class CreateCrossTxProcessor extends CrossChainBaseProcessor {

    @Override
    public String getCommand() {
        return "createcrosstx";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<chainId>  source chainId - require")
                .newLine("\t<formAddress>  source address - require")
                .newLine("\t<toAddress>  target address - require")
                .newLine("\t<assetChainId> transaction asset chainId - require")
                .newLine("\t<assetId> transaction assetId - require")
                .newLine("\t<amount> \t\tamount, you can have up to 8 valid digits after the decimal point - required")
                .newLine("\t[remark] \t\tremark ");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createcrosstx <chainId> <formAddress> <toAddress> <assetChainId> <assetId> <amount> [remark] --create cross chain tx";
    }


    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args, 6, 7);
        checkIsNumeric(args[1], "chainId");
        checkAddress(Integer.parseInt(args[1]), args[2]);
        checkIsNumeric(args[4], "assetChainId");
        checkIsNumeric(args[5], "assetId");
        checkIsNumeric(args[6], "amount");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        Integer chainId = Integer.parseInt(args[1]);
        String formAddress = args[2];
        String toAddress = args[3];
        Integer assetChainId = Integer.parseInt(args[4]);
        Integer assetId = Integer.parseInt(args[5]);
        BigInteger amount = new BigInteger(args[6]);
        String remark = null;
        if (args.length == 8) {
            remark = args[7];
        }
        Result<String> result = crossChainProvider.createCrossTx(
                new CreateCrossTxReq.CreateCrossTxReqBuilder(chainId)
                    .addForm(assetChainId, assetId, formAddress, getPwd(), amount)
                    .addTo(assetChainId, assetId, toAddress, amount)
                    .setRemark(remark).build());
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result.getData());
    }
}
