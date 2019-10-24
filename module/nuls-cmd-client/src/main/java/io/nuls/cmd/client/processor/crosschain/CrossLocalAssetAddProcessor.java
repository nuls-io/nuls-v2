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
package io.nuls.cmd.client.processor.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.AddCrossLocalAssetReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: ljs
 * @Time: 2019-08-06 17:34
 * @Description: 功能描述
 */
@Component
public class CrossLocalAssetAddProcessor extends CrossChainBaseProcessor {

    @Override
    public String getCommand() {
        return "addcrosslocalasset";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<regAddress>  register cross asset address - require")
                .newLine("\t<assetId> add assetId - require");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "addcrosslocalasset <regAddress> <assetChainId> <assetId>--add cross local chain asset";
    }


    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args, 2);
        checkAddress(config.getMainChainId(), args[1]);
        checkIsNumeric(args[2], "assetId");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        Integer assetId = Integer.parseInt(args[2]);

        AddCrossLocalAssetReq req = new AddCrossLocalAssetReq(address, assetId, getPwd());
        Result<String> result = chainManageProvider.addCrossLocalAsset(req);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result.getData());
    }
}
