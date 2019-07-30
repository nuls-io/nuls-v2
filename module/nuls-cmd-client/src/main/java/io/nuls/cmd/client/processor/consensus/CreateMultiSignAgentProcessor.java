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

package io.nuls.cmd.client.processor.consensus;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.consensus.facade.CreateAgentReq;
import io.nuls.base.api.provider.transaction.facade.MultiSignTransferRes;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Component;

import java.math.BigInteger;

/**
 * @author: zhoulijun
 * 通过多签地址创建共识节点
 */
@Component
public class CreateMultiSignAgentProcessor extends ConsensusBaseProcessor implements CommandProcessor {


    @Override
    public String getCommand() {
        return "createmultisignagent";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<agentAddress>   agent owner address   -required")
                .newLine("\t<packingAddress>    packing address    -required")
                .newLine("\t<commissionRate>    commission rate (10~100), you can have up to 2 valid digits after the decimal point  -required")
                .newLine("\t<deposit>  amount you want to deposit, you can have up to 8 valid digits after the decimal point -required")
                .newLine("\t[signAddress] first sign account -- not required")
                .newLine("\t[rewardAddress]  Billing address    -not required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createmultisigneagent <agentAddress> <packingAddress> <commissionRate> <deposit> [signAddress] [rewardAddress] --create a agent";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,4,5,6);
        checkIsNumeric(args[3],"commissionRate");
        checkArgs(()->{
            BigInteger commission = new BigInteger(args[3]);
            //commission 取值范围 10~100
            return commission.compareTo(BigInteger.TEN) >= 0 && commission.compareTo(BigInteger.valueOf(100L)) <= 0;
        },"commission rate (10~100)");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String agentAddress = args[1];

        String packingAddress = args[2];
        Integer commissionRate = Integer.parseInt(args[3]);

        BigInteger deposit = config.toSmallUnit(args[4]);
        String rewardAddress = null;
        if(args.length == 7){
            rewardAddress = args[6];
        }
        CreateAgentReq req = new CreateAgentReq(agentAddress,packingAddress,rewardAddress,commissionRate,deposit);
        if(args.length == 6){
            String signAddress = args[5];
            String password = getPwd();
            req.setSignAddress(signAddress);
            req.setPassword(password);
        }
        Result<MultiSignTransferRes> result = consensusProvider.createAgentForMultiSignAccount(req);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result);
    }
}
