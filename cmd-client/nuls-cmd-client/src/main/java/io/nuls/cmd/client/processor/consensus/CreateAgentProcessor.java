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

import io.nuls.api.provider.Result;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.consensus.ConsensusProvider;
import io.nuls.api.provider.consensus.facade.CreateAgentReq;
import io.nuls.base.basic.AddressTool;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.Config;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;

import static io.nuls.cmd.client.CommandHelper.getPwd;

/**
 * @author: zhoulijun
 */
@Component
@Slf4j
public class CreateAgentProcessor implements CommandProcessor {

    @Autowired Config config;

    ConsensusProvider consensusProvider = ServiceManager.get(ConsensusProvider.class);


    @Override
    public String getCommand() {
        return "createagent";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<agentAddress>   agent owner address   -required")
                .newLine("\t<packingAddress>    packing address    -required")
                .newLine("\t<commissionRate>    commission rate (10~100), you can have up to 2 valid digits after the decimal point  -required")
                .newLine("\t<deposit>   amount you want to deposit, you can have up to 8 valid digits after the decimal point -required")
                .newLine("\t[rewardAddress]  Billing address    -not required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createagent <agentAddress> <packingAddress> <commissionRate> <deposit> [rewardAddress] --create a agent";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length < 5 || length > 6){
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if(!AddressTool.validAddress(config.getChainId(),args[1]) || !AddressTool.validAddress(config.getChainId(),args[2]) || StringUtils.isBlank(args[3])
                || StringUtils.isBlank(args[4])){
            return false;
        }
        if(!StringUtils.isNumberGtZeroLimitTwo(args[3])){
            return false;
        }
        if(!StringUtils.isNuls(args[4])){
            return false;
        }
        if(length == 6 && !AddressTool.validAddress(config.getChainId(),args[5])){
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String agentAddress = args[1];
        String password = getPwd("Enter agent address password");
        String packingAddress = args[2];
        Integer commissionRate = Integer.parseInt(args[3]);
        BigInteger deposit = new BigInteger(args[4]);
        String rewardAddress = null;
        if(args.length == 6){
            rewardAddress = args[5];
        }
        CreateAgentReq req = new CreateAgentReq(agentAddress,packingAddress,rewardAddress,commissionRate,deposit,password);
        Result<String> result = consensusProvider.createAgent(req);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result);
    }
}
