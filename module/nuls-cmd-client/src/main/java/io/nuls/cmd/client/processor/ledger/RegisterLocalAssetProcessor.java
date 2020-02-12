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
import io.nuls.base.api.provider.ledger.facade.RegLocalAssetReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.CommandGroup;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.Map;

/**
 * @Author: ljs
 * @Time: 2019-08-06 17:34
 * @Description: 功能描述
 */
@Component
public class RegisterLocalAssetProcessor implements CommandProcessor {
    @Autowired
    Config config;

    LedgerProvider ledgerProvider = ServiceManager.get(LedgerProvider.class);


    @Override
    public CommandGroup getGroup() {
        return CommandGroup.Ledger;
    }

    @Override
    public String getCommand() {
        return "registerlocalasset";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<regAddress>  register asset address - require")
                .newLine("\t<assetName> asset name - require")
                .newLine("\t<symbol> asset symbol - require")
                .newLine("\t<initNumber> asset init Number - require")
                .newLine("\t<decimalPlace> asset decimal - require")
                .newLine("\t<assetOwnerAddress> asset owner - require");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "registerlocalasset <regAddress> <assetName> <symbol> <initNumber> <decimalPlace> <assetOwnerAddress>--add local chain asset";
    }


    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args, 6);
        checkAddress(config.getMainChainId(), args[1]);
        checkIsAmount(args[4], "initNumber");
        checkIsAmount(args[5], "decimalPlace");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        String assetName = args[2];
        String symbol = args[3];
        long initNumber = Long.valueOf(args[4]);
        int decimalPlaces = Integer.parseInt(args[5]);
        String assetOwnerAddress = args[6];
        RegLocalAssetReq req = new RegLocalAssetReq(address,
                symbol, assetName, initNumber, decimalPlaces, getPwd(), assetOwnerAddress);
        Result<Map> result = ledgerProvider.regLocalAsset(req);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result);
    }
}
