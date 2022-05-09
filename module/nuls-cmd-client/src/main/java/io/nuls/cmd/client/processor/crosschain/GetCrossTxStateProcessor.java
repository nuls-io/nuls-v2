package io.nuls.cmd.client.processor.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.GetCrossTxStateReq;
import io.nuls.base.data.Address;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.log.Log;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rockdb.service.RocksDBService;

import java.util.HashSet;
import java.util.Set;

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
                .newLine("\t<txHash>  tx hash - require")
                .toString();
    }

    @Override
    public String getCommandDescription() {
        return getCommand() + " <chainId> <txHash> ";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1);
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        Integer chainId = config.getChainId();
        String txHash = args[1];
        GetCrossTxStateReq req = new GetCrossTxStateReq(chainId,txHash);
        Result<Integer> result = crossChainProvider.getCrossTxState(req);
        Result<Transaction> resultTxInfo = crossChainProvider.getCrossTx(req);
        signList(resultTxInfo.getData());
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        String state = result.getData() + "(0:Unconfirmed  1:MainNetConfirmed  2:Confirmed)";
        return CommandResult.getSuccess(state);
    }

    public void signList(Transaction tx) {
        try{
            TransactionSignature signature = new TransactionSignature();
            signature.parse(tx.getTransactionSignature(), 0);
            Log.debug("txHex:{}", HexUtil.encode(tx.serialize()));
            Log.debug("txType:{}",tx.getType());
            Log.debug("txSignCount:{}", signature.getSignersCount());
            Set<String> addressSets = new HashSet<>();
            Log.debug("signer:");
            signature.getP2PHKSignatures().forEach(sign -> {
                Address address = new Address(config.getChainId(), config.getAddressPrefix(), BaseConstant.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(sign.getPublicKey()));
                addressSets.add(address.getBase58());
            });
            addressSets.forEach((addr)->Log.debug("{}",addr));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
