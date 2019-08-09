/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.cmd.client.processor.transaction;

import io.nuls.base.api.provider.Result;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

/**
 * @author: Charlie
 * @date: 2019/6/29
 */
@Component
public class TransferTestProcessor extends TransactionBaseProcessor implements CommandProcessor {

    @Autowired
    Config config;

    @Override
    public String getCommand() {
        return "transferTest";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
//        builder.newLine(getCommandDescription())
//                .newLine("\t<method> \t\t用哪个方法发送交易 1:发送固定50W笔 2:两万账户互发- Required")
//                .newLine("\t<address1> \t有钱地址1 - Required")
//                .newLine("\t[address2] \t\t有钱地址2 两万账户互发时必填 - Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "transferTest <method> <address1> [address2] --transfer test";
    }

    @Override
    public boolean argsValidate(String[] args) {
        return true;
    }


    @Override
    public CommandResult execute(String[] args) {
        Integer method = Integer.parseInt(args[1]);
        String address1 = args[2];
        String address2 = null;
        if(method == 2) {
            address2 = args[3];
        }

        Result<String> result = transferService.transferTest(method, address1, address2);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result.getData());
    }


}
