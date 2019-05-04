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

package io.nuls.cmd.client.processor.contract;


import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.contract.facade.DeleteContractReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.ErrorCodeConstants;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.StringUtils;

/**
 * Delete contract
 * Created by wangkun23 on 2018/9/21.
 */
@Component
public class DeleteContractProcessor extends ContractBaseProcessor {


    @Override
    public String getCommand() {
        return "deletecontract";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> contract address -required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "deletecontract <sender> <address> --delete contract";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,2);
        checkAddress(config.getChainId(),args[1],args[2]);
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String sender = args[1];
        if (StringUtils.isBlank(sender)) {
            return CommandResult.getFailed(ErrorCodeConstants.PARAM_ERR.getMsg());
        }
        String password = CommandHelper.getPwd();
        String contractAddress = args[2];
        Result<String> result = contractProvider.deleteContract(new DeleteContractReq(sender,contractAddress,password));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(result);
    }
}
