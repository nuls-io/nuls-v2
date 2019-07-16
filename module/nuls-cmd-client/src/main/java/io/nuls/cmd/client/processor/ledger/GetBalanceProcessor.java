/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.cmd.client.processor.ledger;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.ledger.LedgerProvider;
import io.nuls.base.api.provider.ledger.facade.AccountBalanceInfo;
import io.nuls.base.api.provider.ledger.facade.GetBalanceReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.CommandGroup;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.cmd.client.utils.Na;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: zhoulijun
 */
@Component
public class GetBalanceProcessor implements CommandProcessor {

    LedgerProvider ledgerProvider = ServiceManager.get(LedgerProvider.class);

    @Autowired
    Config config;

    @Override
    public String getCommand() {
        return "getbalance";
    }

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.Ledger;
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> the account address - require")
                .newLine("\t[assetChainId] the asset chain id - require")
                .newLine("\t<assetId> the asset id - require");
        return builder.toString();
    }
    @Override
    public String getCommandDescription() {
        return "getbalance <address> [assetChainId] [assetId]--get the balance of a address";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1,3);
        checkAddress(config.getChainId(),args[1]);
        if(args.length == 4){
            checkIsNumeric(args[2],"asset chain id ");
            checkIsNumeric(args[3],"asset id");
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        Integer assetChainId = config.getChainId();
        Integer assetId = config.getAssetsId();
        if(args.length == 4){
            assetChainId = Integer.parseInt(args[2]);
            assetId = Integer.parseInt(args[3]);
        }
        Result<AccountBalanceInfo> result = ledgerProvider.getBalance(new GetBalanceReq(assetId,assetChainId,address));
        if(result.isFailed()){
            return CommandResult.getFailed(result);
        }
        Map<String,Object> r = new HashMap<>(3);
        r.put("available",config.toBigUnit(result.getData().getAvailable()));
        r.put("freeze",config.toBigUnit(result.getData().getFreeze()));
        r.put("total",config.toBigUnit(result.getData().getTotal()));
        return CommandResult.getSuccess(new Result(r));
    }


    public static void main(String[] args) {
        System.out.println(BigInteger.TEN.pow(10));
    }
}
