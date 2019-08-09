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

package io.nuls.cmd.client.processor.transaction;


import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.transaction.facade.TransferReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.StringUtils;

import java.math.BigInteger;

import static io.nuls.cmd.client.CommandHelper.getPwd;

/**
 * @author: zhoulijun
 */
@Component
@Deprecated
public class TransferByAliasProcessor  extends TransactionBaseProcessor implements CommandProcessor {


    @Autowired
    Config config;

    @Override
    public String getCommand() {
        return "transferbyalias";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<alias> \t\tsource alias - Required")
                .newLine("\t<toaddress> \treceiving address - Required")
                .newLine("\t<amount> \t\tamount, you can have up to 8 valid digits after the decimal point - Required")
                .newLine("\t[remark] \t\tremark - ");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "transferbyalias <alias> <toAddress> <amount> [remark] --transfer";
    }

    @Override
    public boolean argsValidate(String[] args) {
        boolean result;
        do {
            int length = args.length;
            if (length != 4 && length != 5) {
                result = false;
                break;
            }
            if (!CommandHelper.checkArgsIsNull(args)) {
                result = false;
                break;
            }

            if (!StringUtils.isNuls(args[3])) {
                result = false;
                break;
            }
//            TransferForm form = getTransferForm(args);
//            if(null == form){
//                result = false;
//                break;
//            }
//            paramsData.set(form);
//            result = StringUtils.isNotBlank(form.getToAddress());
//            if (!result) {
//                break;
//            }
            BigInteger amount = new BigInteger(args[3]);
            result = amount.compareTo(BigInteger.ZERO) > 0;
        } while (false);
        return result;
    }

    private TransferReq buildTransferReq(String[] args) {
        String formAddress = args[1];
        String toAddress = args[2];
        BigInteger amount = new BigInteger(args[3]);
        TransferReq.TransferReqBuilder builder =
                new TransferReq.TransferReqBuilder(config.getChainId(),config.getAssetsId())
                        .addForm(formAddress,getPwd("Enter your account password"), amount)
                        .addTo(toAddress,amount);
        if(args.length == 5){
            builder.setRemark(args[4]);
        }
        return builder.build(new TransferReq());
    }

    @Override
    public CommandResult execute(String[] args) {
//        String alias = args[1];
//        String toAddress = args[2];
//        BigInteger amount = Na.parseNuls(args[3]).toBigInteger();
//        String remark = null;
//        if(args.length > 4){
//            remark = args[4];
//        }
//        String password = getPwd("Enter your account password");
        Result<String> result = transferService.transferByAlias(buildTransferReq(args));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result.getData());
    }
}
