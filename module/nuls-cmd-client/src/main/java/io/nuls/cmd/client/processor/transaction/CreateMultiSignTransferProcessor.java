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
import io.nuls.base.api.provider.transaction.facade.CreateMultiSignTransferReq;
import io.nuls.base.api.provider.transaction.facade.MultiSignTransferRes;
import io.nuls.base.api.provider.transaction.facade.TransferReq;
import io.nuls.base.basic.AddressTool;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author: zhoulijun
 */
@Component
public class CreateMultiSignTransferProcessor extends TransactionBaseProcessor implements CommandProcessor {

    @Autowired
    Config config;


    @Override
    public String getCommand() {
        return "createmultisigntransfer";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> \t\tsource address or alias - Required")
                .newLine("\t<toaddress> \treceiving address or alias - Required")
                .newLine("\t<amount> \t\tamount, you can have up to 8 valid digits after the decimal point - Required")
                .newLine("\t[remark] \t\tremark - ");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createmultisigntransfer <multi sign address> <toAddress>|<alias> <amount> [remark] ";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,3,4);
        checkIsAmount(args[3],"amount");
        return true;
    }

    private CreateMultiSignTransferReq buildTransferReq(String[] args) {
        String formAddress = args[1];
        String toAddress = args[2];
        BigInteger amount = config.toSmallUnit(new BigDecimal(args[3]));
        CreateMultiSignTransferReq.TransferReqBuilder<CreateMultiSignTransferReq> builder =
                new TransferReq.TransferReqBuilder(config.getChainId(),config.getAssetsId())
                        .addForm(formAddress,null, amount)
                        .addTo(toAddress,amount);
        if(args.length == 5){
            builder.setRemark(args[4]);
        }
        return builder.build(new CreateMultiSignTransferReq());
    }

    @Override
    public CommandResult execute(String[] args) {
        Result<MultiSignTransferRes> result = transferService.multiSignTransfer(buildTransferReq(args));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result);
    }

    public static void main(String[] args) {
        if (!AddressTool.validAddress(2, "tNULSeBaNQM6FzaviJbzxQ8FkwtajaVhgrTvHT")) {
            System.out.println("fail");
        }else {
            System.out.println("success");
        }

    }

}
