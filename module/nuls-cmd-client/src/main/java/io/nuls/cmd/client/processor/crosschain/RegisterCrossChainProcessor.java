package io.nuls.cmd.client.processor.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.RegisterChainReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.core.core.annotation.Component;

import java.math.BigInteger;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-07 10:44
 * @Description: 功能描述
 */
@Component
public class RegisterCrossChainProcessor extends CrossChainBaseProcessor {

    public static final BigInteger MAX_MAGIC_NUMBER = BigInteger.valueOf(4294967295L);

    @Override
    public String getCommand() {
        return "registercrosschain";
    }

    @Override
    public String getHelp() {
        return new CommandBuilder()
                .newLine(getCommandDescription())
                .newLine("\t<address>  payment main chain address - require")
                .newLine("\t<chainId>  register chain id - require")
                .newLine("\t<chainName>  register chain name - require")
                .newLine("\t<magicNumber> chain connect magic number - require")
                .newLine("\t<assetId> register assetId - require")
                .newLine("\t<symbol>  register asset symbol - required")
                .newLine("\t<assetName>  register asset name - required")
                .newLine("\t<initNumber>  register asset circulation - required")
                .newLine("\t[decimalPlaces]  register asset decimal digits，default 8 ")
                .newLine("\t[minAvailableNodeNum]  cross chain tx rely on min node number，default 5 ")
                .newLine("\t[txConfirmedBlockNum]  cross chain tx success rely on confirm block number，default 30 ")
                .toString();
    }

    @Override
    public String getCommandDescription() {
        return getCommand() + " <address> <chainId> <chainName> <magicNumber> <assetId> <symbol> <assetName> <initNumber> [decimalPlaces] [minAvailableNodeNum] [txConfirmedBlockNum]";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args, 8, 9, 10, 11);
        checkAddress(config.getMainChainId(), args[1]);
        checkIsNumeric(args[2], "chainId");
        checkIsNumeric(args[4], "magicNumber");
        BigInteger magicNumber = new BigInteger(args[4]);
        checkArgs(magicNumber.min(MAX_MAGIC_NUMBER).equals(magicNumber) && !magicNumber.min(BigInteger.ZERO).equals(magicNumber), "magic number must be greater than 0 is less than " + MAX_MAGIC_NUMBER);
        checkIsNumeric(args[5], "assetId");
        checkIsNumeric(args[8], "initNumber");
        if (args.length > 9) {
            checkIsNumeric(args[9], "decimalPlaces");
        }
        if (args.length > 10) {
            checkIsNumeric(args[10], "minAvailableNodeNum");
        }
        if (args.length > 11) {
            checkIsNumeric(args[11], "txConfirmedBlockNum");
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        Integer chainId = Integer.parseInt(args[2]);
        String chainName = args[3];
        Long magicNumber = Long.parseLong(args[4]);
        Integer assetId = Integer.parseInt(args[5]);
        String symbol = args[6];
        String assetName = args[7];
        Long initNumber = Long.parseLong(args[8]);
        int decimalPlaces = 8;
        int minAvailableNodeNum = 5;
        int txConfirmedBlockNum = 30;
        if (args.length > 9) {
            decimalPlaces = Integer.parseInt(args[9]);
        }
        if (args.length > 10) {
            minAvailableNodeNum = Integer.parseInt(args[10]);
        }
        if (args.length > 11) {
            txConfirmedBlockNum = Integer.parseInt(args[11]);
        }
        RegisterChainReq req = new RegisterChainReq(address,chainId,chainName,magicNumber,assetId,symbol,assetName,initNumber,"1",getPwd());
        req.setDecimalPlaces(decimalPlaces);
        req.setMinAvailableNodeNum(minAvailableNodeNum);
        req.setTxConfirmedBlockNum(txConfirmedBlockNum);
        Result<String> result = chainManageProvider.registerChain(req);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result.getData());
    }
}
