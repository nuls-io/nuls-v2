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
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.consensus.ConsensusProvider;
import io.nuls.base.api.provider.consensus.facade.DepositToAgentReq;
import io.nuls.base.data.NulsDigestData;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.cmd.client.utils.Na;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.math.BigInteger;

import static io.nuls.cmd.client.CommandHelper.getPwd;

/**
 * @author: zhoulijun
 */
@Component
public class DepositProcessor extends ConsensusBaseProcessor implements CommandProcessor {

    ConsensusProvider consensusProvider = ServiceManager.get(ConsensusProvider.class);

    @Autowired
    Config config;
    @Override
    public String getCommand() {
        return "deposit";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<address>   Your own account address -required")
                .newLine("\t<agentHash>   The agent hash you want to deposit  -required")
                .newLine("\t<deposit>   the amount you want to deposit, you can have up to 8 valid digits after the decimal point -required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "deposit <address> <agentHash> <deposit> --apply for deposit";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,3);
        checkAddress(config.getChainId(),args[1]);
        checkIsNumeric(args[3],"deposit");
        checkArgs(NulsDigestData.validHash(args[2]),"agentHash format error");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        String password = getPwd();
        BigInteger deposit = Na.parseNuls(args[3]).toBigInteger();
        String agentHash = args[2];
        Result<String> result = consensusProvider.depositToAgent(new DepositToAgentReq(address,agentHash,deposit,password));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result);
    }
}
