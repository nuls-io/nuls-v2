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
import io.nuls.base.api.provider.consensus.facade.StopAgentReq;
import io.nuls.base.api.provider.consensus.facade.StopMultiSignAgentReq;
import io.nuls.base.api.provider.transaction.facade.MultiSignTransferRes;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

/**
 * @author: zhoulijun
 * Stop a consensus node created by a multi signed account
 */
@Component
public class StopMultiSignAgentProcessor extends ConsensusBaseProcessor implements CommandProcessor {

    @Autowired
    Config config;

    ConsensusProvider consensusProvider = ServiceManager.get(ConsensusProvider.class);

    @Override
    public String getCommand() {
        return "stopmultisignagent";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<address> account address of the agent -required")
                .newLine("\t[sign address] first sign address for transfer -- not required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "stopmultisignagent <address> [sign address]  -- stop the multi sign agent";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1,2);
        checkAddress(config.getChainId(),args[1]);
        if(args.length == 3){
            checkAddress(config.getChainId(),args[2]);
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        StopMultiSignAgentReq req = new StopMultiSignAgentReq(address);
        if(args.length == 3){
            String signAddress = args[2];
            String password = getPwd();
            req.setSignAddress(signAddress);
            req.setPassword(password);
        }
        Result<MultiSignTransferRes> result = consensusProvider.stopAgentForMultiSignAccount(req);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result);
    }
}
