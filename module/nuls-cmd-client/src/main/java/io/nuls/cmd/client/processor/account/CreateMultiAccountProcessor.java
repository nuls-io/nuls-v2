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
import io.nuls.base.api.provider.account.facade.GenerateMultiSignAccountReq;
import io.nuls.base.basic.AddressTool;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.StringUtils;

import java.util.Arrays;

/**
 * @author zhoulijun
 */
@Component
public class CreateMultiAccountProcessor extends AccountBaseProcessor implements CommandProcessor {

    @Override
    public String getCommand() {
        return "createmultisignaccount";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<pubKeys> 参与多签的公钥列表，多个用\",\"隔开")
                .newLine("\t[minSigns] 最小签名数量，默认全部");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createmultisignaccount <pubKeys> --create multi sign account";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1,2);
        if(args.length==2){
            return true;
        }
        checkArgs(StringUtils.isNumeric(args[2]),"must enter a number");
        checkArgs(Integer.parseInt(args[2]) > 0,"must be greater than zero");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {

        GenerateMultiSignAccountReq req = new GenerateMultiSignAccountReq();
        req.setPubKeys(Arrays.asList(args[1].split(",")));
        if(args.length == 3){
            req.setMinSigns(Integer.parseInt(args[2]));
        }else{
            req.setMinSigns(req.getPubKeys().size());
        }
        Result<String> result = accountService.createMultiSignAccount(req);
        if(!result.isSuccess()){
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(result);
    }

    public static void main(String[] args) {
        System.out.println(AddressTool.isMultiSignAddress("tNULSeBaMt7BiJcdHNXxpos7gDt6XpwY15NKmi"));
    }

}
