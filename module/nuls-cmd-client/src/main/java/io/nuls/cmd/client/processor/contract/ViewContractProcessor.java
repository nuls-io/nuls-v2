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

package io.nuls.cmd.client.processor.contract;


import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.contract.facade.ViewContractReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.core.core.annotation.Component;

import java.util.Map;


/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/9/19
 */
@Component
public class ViewContractProcessor extends ContractBaseProcessor {


    private ThreadLocal<ViewContractReq> paramsData = new ThreadLocal<>();

    @Override
    public String getCommand() {
        return "viewcontract";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<contractAddress>   contract address    -required")
                .newLine("\t<methodName>        the method to call    -required")
                .newLine("\t[-d methodDesc]        the method description    -not required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "viewcontract <contractAddress> <methodName> [-d methodDesc] --view contract";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,2,3);
        checkAddress(config.getChainId(),args[1]);
        ViewContractReq form = getContractViewCall(args);
        if(null == form){
            return false;
        }
        paramsData.set(form);
        return true;
    }

    private ViewContractReq getContractViewCall(String[] args) {
        ViewContractReq call;
        try {
            call = new ViewContractReq();
            call.setContractAddress(args[1].trim());
            call.setMethodName(args[2].trim());

            if(args.length == 5) {
                String argType = args[3].trim();
                if ("-d".equals(argType)) {
                    call.setMethodDesc(args[4].trim());
                } else {
                    return null;
                }
            }
            return call;
        } catch (Exception e) {
            e.fillInStackTrace();
            return null;
        }
    }


    @Override
    public CommandResult execute(String[] args) {
        ViewContractReq form = paramsData.get();
        if (null == form) {
            form = getContractViewCall(args);
        }
        if (null == form) {
            return CommandResult.getFailed("parameter error.");
        }
        form.setArgs(getContractCallArgsJson());

//        Map<String, Object> parameters = new HashMap<>();
//        parameters.put("contractAddress", form.getContractAddress());
//        parameters.put("methodName", form.getMethodName());
//        parameters.put("methodDesc", form.getMethodDesc());
//        parameters.put("args", contractArgs);
//        RpcClientResult result = restFul.post("/contract/view", parameters);
        Result<Map> result = contractProvider.viewContract(form);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(result);
    }


}
