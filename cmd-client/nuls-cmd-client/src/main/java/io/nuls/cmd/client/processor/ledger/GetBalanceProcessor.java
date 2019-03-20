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

import io.nuls.api.provider.Result;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.ledger.LedgerProvider;
import io.nuls.api.provider.ledger.facade.AccountBalanceInfo;
import io.nuls.api.provider.ledger.facade.GetBalanceReq;
import io.nuls.base.basic.AddressTool;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.Config;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.cmd.client.processor.CommandGroup;
import io.nuls.cmd.client.utils.Na;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: zhoulijun
 */
@Component
@Slf4j
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
                .newLine("\t<address> the account address - require");
        return builder.toString();
    }
    @Override
    public String getCommandDescription() {
        return "getbalance <address> --get the balance of a address";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length != 2) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if(!AddressTool.validAddress(config.getChainId(),args[1])){
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        Result<AccountBalanceInfo> result = ledgerProvider.getBalance(new GetBalanceReq(config.getAssetsId(),config.getChainId(),address));
        if(result.isFailed()){
            return CommandResult.getFailed(result);
        }
        Map<String,Object> r = new HashMap<>(3);
        r.put("available",Na.valueOf(result.getData().getAvailable()).toNuls());
        r.put("freeze",Na.valueOf(result.getData().getFreeze()).toNuls());
        r.put("total",Na.valueOf(result.getData().getTotal()).toNuls());
        return CommandResult.getSuccess(new Result(r));
    }
}
