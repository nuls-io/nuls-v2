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
    public static final int MAX_BFT_RATIO = 100;
    public static final int MIN_BFT_RATIO = 66;

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
                .newLine("\t<addressPrefix>  register chain address prefix.1-5 char - require")
                .newLine("\t<magicNumber> chain connect magic number - require")
                .newLine("\t<maxSignatureCount> chain cross verifier max number - require")
                .newLine("\t<signatureBFTRatio> cross tx BFT ratio >=66 <=100 - require")
                .newLine("\t<verifierList> verifier address list,split by comma - require")
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
        return getCommand() + " <address> <chainId> <chainName> <addressPrefix> <magicNumber> <maxSignatureCount> <signatureBFTRatio> <verifierList> <assetId> <symbol> <assetName> <initNumber> [decimalPlaces] [minAvailableNodeNum] [txConfirmedBlockNum]";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args, 12, 13, 14, 15);
        checkAddress(config.getMainChainId(), args[1]);
        checkIsNumeric(args[2], "chainId");
        checkIsNumeric(args[5], "magicNumber");
        BigInteger magicNumber = new BigInteger(args[5]);
        checkArgs(magicNumber.min(MAX_MAGIC_NUMBER).equals(magicNumber) && !magicNumber.min(BigInteger.ZERO).equals(magicNumber), "magic number must be greater than 0 is less than " + MAX_MAGIC_NUMBER);
        int signatureBFTRatio = Integer.valueOf(args[7]);
        checkArgs(signatureBFTRatio >= MIN_BFT_RATIO && signatureBFTRatio <= MAX_BFT_RATIO, "cross tx BFT ratio >=" + MIN_BFT_RATIO + "<=" + MAX_BFT_RATIO);
        checkIsNumeric(args[6], "maxSignatureCount");
        checkIsNumeric(args[7], "signatureBFTRatio");
        checkIsNumeric(args[9], "assetId");
        checkIsNumeric(args[12], "initNumber");
        if (args.length > 13) {
            checkIsNumeric(args[13], "decimalPlaces");
        }
        if (args.length > 14) {
            checkIsNumeric(args[14], "minAvailableNodeNum");
        }
        if (args.length > 14) {
            checkIsNumeric(args[14], "txConfirmedBlockNum");
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        Integer chainId = Integer.parseInt(args[2]);
        String chainName = args[3];
        String addressPrefix = args[4];
        Long magicNumber = Long.parseLong(args[5]);
        int maxSignatureCount = Integer.parseInt(args[6]);
        int signatureBFTRatio = Integer.parseInt(args[7]);
        String verifierList = args[8];

        Integer assetId = Integer.parseInt(args[9]);
        String symbol = args[10];
        String assetName = args[11];
        Long initNumber = Long.parseLong(args[12]);
        int decimalPlaces = 8;
        int minAvailableNodeNum = 5;
        int txConfirmedBlockNum = 30;
        if (args.length > 13) {
            decimalPlaces = Integer.parseInt(args[13]);
        }
        if (args.length > 14) {
            minAvailableNodeNum = Integer.parseInt(args[14]);
        }
        if (args.length > 14) {
            txConfirmedBlockNum = Integer.parseInt(args[15]);
        }
        RegisterChainReq req = new RegisterChainReq(address, chainId, chainName, addressPrefix, magicNumber,
                maxSignatureCount, signatureBFTRatio, verifierList,
                assetId, symbol, assetName, initNumber, "1", getPwd());
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
