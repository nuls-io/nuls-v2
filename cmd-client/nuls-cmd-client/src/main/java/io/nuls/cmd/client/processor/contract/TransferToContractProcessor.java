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


import io.nuls.api.provider.Result;
import io.nuls.api.provider.contract.facade.TransferToContractReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.ErrorCodeConstants;
import io.nuls.cmd.client.utils.Na;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.model.StringUtils;

import java.math.BigInteger;

/**
 * Transfer to contract address
 * Created by wangkun23 on 2018/9/25.
 */
@Component
public class TransferToContractProcessor extends ContractBaseProcessor {

    @Override
    public String getCommand() {
        return "transfertocontract";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> address -required")
                .newLine("\t<toAddress> toAddress -required")
                .newLine("\t<amount> transfer amount -required")
                .newLine("\t[remark] remark not -required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "transfertocontract <address> <toAddress> <amount> [remark] --create transfer to contract address";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length <5) {
            return false;
        }
        if (length >6) {
            return false;
        }
        return true;
    }

    /**
     * {
     * "address": "Nsdv1Hbu4TokdgbXreypXmVttYKdPT1g",
     * "toAddress": "NseDqffhWEB52a9cWfiyEhiP3wPGcjcJ",
     * "password": "nuls123456",
     * "amount": 10000000,
     * "remark": ""
     * }
     *
     * @param args
     * @return
     */
    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        if (StringUtils.isBlank(address)) {
            return CommandResult.getFailed(ErrorCodeConstants.PARAM_ERR.getMsg());
        }
        String password = CommandHelper.getPwd("Please enter your account password");
        String toAddress = args[2];
        BigInteger amount = Na.parseNuls(args[3]).toBigInteger();
        TransferToContractReq req = new TransferToContractReq(address,toAddress,amount,password,null);
        Result<String> result = contractProvider.transferToContract(req);
        if(args.length > 4){
            req.setRemark(args[4]);
        }
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(result);
    }
}
