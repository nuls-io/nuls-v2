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
import io.nuls.base.api.provider.account.facade.CreateAccountReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.StringUtils;

/**
 * @author zhoulijun
 */
@Component
public class CreateProcessor extends AccountBaseProcessor implements CommandProcessor {

    @Override
    public String getCommand() {
        return "create";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t[number] The count of accounts you want to create, - default 1");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "create [number] --create account, [number] the number of accounts you want to create, - default 1";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,0,1);
        if(args.length==1){
            return true;
        }
        checkArgs(StringUtils.isNumeric(args[1]),"must enter a number");
        checkArgs(Integer.parseInt(args[1]) > 0,"must be greater than zero");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String password = getPwd("Please enter the new password(8-20 characters, the combination of letters and numbers).\nEnter your new password:");
        if(StringUtils.isNotBlank(password)){
            CommandHelper.confirmPwd(password);
        }
        int count = 1;
        if(args.length == 2){
            count = Integer.parseInt(args[1]);
        }
        Result<String> result = accountService.createAccount(new CreateAccountReq(count,password));
        if(!result.isSuccess()){
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(result);
    }
}
