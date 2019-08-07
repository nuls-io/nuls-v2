/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.cmd.client.processor.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.AddCrossAssetReq;
import io.nuls.base.api.provider.crosschain.facade.DisableAssetReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: ljs
 * @Time: 2019-08-06 17:34
 * @Description: 功能描述
 */
@Component
public class CrossAssetAddProcessor extends CrossChainBaseProcessor {

    @Override
    public String getCommand() {
        return "addcrossasset";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<regAddress>  register cross asset address - require")
                .newLine("\t<assetChainId>  add asset chainId - require")
                .newLine("\t<assetId> add assetId - require")
                .newLine("\t<assetName> asset name - require")
                .newLine("\t<symbol> asset symbol - require")
                .newLine("\t<initNumber> asset init Number - require")
                .newLine("\t<decimalPlaces> asset init Number - require");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "addcrossasset <regAddress> <assetChainId> <assetId> <assetName> <symbol> <initNumber> <decimalPlaces>--add cross chain asset";
    }


    @Override
    public boolean argsValidate(String[] args) {
        checkAddress(config.getMainChainId(), args[1]);
        checkIsNumeric(args[2], "assetChainId");
        checkIsNumeric(args[3], "assetId");
        checkIsAmount(args[6], "initNumber");
        checkIsAmount(args[7], "decimalPlaces");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        Integer assetChainId = Integer.parseInt(args[2]);
        Integer assetId = Integer.parseInt(args[3]);
        String assetName = args[4];
        String symbol = args[5];
        long  initNumber = Long.valueOf(args[6]);
        int decimalPlaces = Integer.parseInt(args[3]);
        AddCrossAssetReq req = new AddCrossAssetReq(address, assetChainId, assetId,
                symbol,assetName,initNumber,decimalPlaces,getPwd());
        Result<String> result = chainManageProvider.addCrossAsset(req);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result.getData());
    }
}
