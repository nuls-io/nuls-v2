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

package io.nuls.cmd.client.processor.account;


import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.SetAccountAliasReq;
import io.nuls.base.api.provider.account.facade.SetMultiSignAccountAliasReq;
import io.nuls.base.api.provider.transaction.facade.MultiSignTransferRes;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.FormatValidUtils;

/**
 * @author: zhoulijun
 */
@Component
public class SetMultiSignAliasProcessor extends AccountBaseProcessor implements CommandProcessor {

    @Autowired
    Config config;

    @Override
    public String getCommand() {
        return "setmultisignaccountalias";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<multi sign address> The address of the account, - Required")
                .newLine("\t<alias> The alias of the account, the bytes for the alias is between 1 and 20 " +
                        "(only lower case letters, Numbers and underline, the underline should not be at the begin and end), - Required")
                .newLine("\t[sign address]  ");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "setmultisignaccountalias <address> <alias> [sign address] --Set an alias for the account ";
    }


    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,2,3);
        checkAddress(config.getChainId(),args[1]);
        checkArgs(FormatValidUtils.validAlias(args[2]),"alias format error");
        if(args.length == 4){
            checkAddress(config.getChainId(),args[3]);
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        String alias = args[2];
        SetMultiSignAccountAliasReq req = new SetMultiSignAccountAliasReq(address,alias);
        if(args.length == 4){
            String signAddress = args[3];
            String password = getPwd();
            req.setSignAddress(signAddress);
            req.setSignPassword(password);
        }
        Result<MultiSignTransferRes> result = accountService.setMultiSignAccountAlias(req);
        if(result.isFailed()){
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result);
    }
}
